package com.lemnt.promotion.application.promotion.service;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.promotion.application.coupon.service.CouponLifecycleService;
import com.lemnt.promotion.application.promotion.cmd.CombinationRecordCreateCmd;
import com.lemnt.promotion.client.bo.PromotionLockBo;
import com.lemnt.promotion.client.dto.PromotionLockDTO;
import com.lemnt.promotion.domain.coupon.gateway.CouponGateway;
import com.lemnt.promotion.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.promotion.domain.coupon.model.CouponOrderReference;
import com.lemnt.promotion.domain.event.gateway.MessageInboxGateway;
import com.lemnt.promotion.domain.order.gateway.PromotionOrderGateway;
import com.lemnt.promotion.domain.order.model.PromotionOrderResources;
import com.lemnt.promotion.domain.promotion.gateway.CombinationRecordGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * 资源锚点及各动作凭证与本库券/活动写入同事务，终止凭证防止延迟锁定。
 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
public class PromotionResourceService {
    private final PromotionOrderGateway orders;
    private final MessageInboxGateway receipts;
    private final CouponGateway coupons;
    private final CouponTemplateGateway templates;
    private final CouponLifecycleService couponLifecycle;
    private final SeckillStockService seckill;
    private final PointActivityManagementService point;
    private final BargainActivityManagementService bargain;
    private final BargainRecordService bargainRecords;
    private final CombinationRecordService combination;
    private final CombinationRecordGateway combinationRecords;

    public PromotionLockDTO lock(PromotionLockBo input) {
        if (input == null) throw new IllegalArgumentException("资源冻结请求不能为空");
        var plan = plan(input.orderNo());
        if (!frozen(plan).equals(input)) throw new IllegalStateException("资源请求与原订单不一致");
        fence(plan);
        if (has("rollback", plan.orderNo())) return state(plan);
        if (has("lock", plan.orderNo())) return state(plan);
        if (!Set.of("RESERVING", "PENDING_PAYMENT").contains(plan.status()))
            throw new IllegalStateException("订单状态不允许首次冻结资源");
        if (plan.couponId() != null) {
            var coupon = coupons.coupon(plan.memberId(), plan.couponId(), true);
            var now = templates.now();
            if (coupon == null
                || coupon.terms() == null
                || !"AVAILABLE".equals(coupon.status())
                || now.isBefore(coupon.terms().validFrom())
                || !now.isBefore(coupon.terms().validUntil())) {
                throw new IllegalStateException("原优惠券不可锁定；存量冻结资源缺失凭证时须先审核迁移");
            }
            coupons.lockCoupon(plan.memberId(), plan.couponId(), plan.orderNo());
        }
        if (plan.activityType() != null) {
            for (var line : plan.lines())
                decrease(plan.activityType(), plan.activityId(), line.skuId(), line.quantity());
            if ("BARGAIN".equals(plan.activityType()))
                bargainRecords.bindOrder(plan.activityRecordId(), plan.orderId());
        }
        action("lock", plan);
        return state(plan);
    }

    public PromotionLockDTO confirm(String no) {
        var plan = plan(no);
        fence(plan);
        if (has("paid", no)) return state(plan);
        if (has("rollback", no)
            || !has("lock", no)
            || !Set.of("PAID", "SHIPPED", "COMPLETED", "REFUNDED").contains(plan.status()))
            throw new IllegalStateException("支付资源前置状态未完成或订单已关闭");
        if (plan.couponId() != null) {
            var coupon = coupons.byOrderNo(no);
            if (coupon == null
                || !plan.memberId().equals(coupon.memberId())
                || !plan.couponId().equals(coupon.id()))
                throw new IllegalStateException("原订单券绑定缺失");
        }
        couponLifecycle.useCoupon(no);
        if ("COMBINATION".equals(plan.activityType()) && recordId(plan) == null) {
            if (plan.lines().size() != 1) throw new IllegalStateException("拼团订单商品行不唯一");
            var line = plan.lines().get(0);
            var record =
                combination.create(
                    new CombinationRecordCreateCmd(
                        plan.memberId(),
                        line.spuId(),
                        plan.activityId(),
                        plan.headId(),
                        line.skuId(),
                        line.quantity()));
            combination.bindOrder(record.id(), plan.orderId());
        }
        action("paid", plan);
        return state(plan);
    }

    public void rollback(String no) {
        var plan = plan(no);
        fence(plan);
        if (!"CLOSED".equals(plan.status()) || has("paid", no))
            throw new IllegalStateException("只有未支付关单可以释放资源");
        if (has("rollback", no)) return;
        if (has("lock", no)) {
            for (var line : plan.lines())
                restore(plan.activityType(), plan.activityId(), line.skuId(), line.quantity());
            var coupon = coupons.byOrderNo(no);
            if (coupon != null)
                coupons.releaseCoupon(new CouponOrderReference(plan.memberId(), coupon.id(), no));
        }
        action("rollback", plan);
    }

    public boolean refundApplied(String no) {
        return receipts.has("promotion.after_sale_completed.v1", "AFTER_SALE_COMPLETED", no);
    }

    public PromotionLockDTO query(String no) {
        var plan = plan(no);
        fence(plan);
        return state(plan);
    }

    /**
     * 已冻结售后按行回补活动库存，商品库存和会员积分由各自服务处理。
     */
    public void refund(String no, String afterSaleNo, Long skuId, int quantity, boolean full) {
        var plan = plan(no);
        fence(plan);
        if (!has("paid", no)) throw new IllegalStateException("原支付权益尚未处理，请重试售后事件");
        if (quantity < 1
            || plan.lines().stream()
            .noneMatch(l -> skuId.equals(l.skuId()) && quantity <= l.quantity()))
            throw new IllegalArgumentException("售后行不属于原资源计划");
        restore(plan.activityType(), plan.activityId(), skuId, quantity);
        if (full) couponLifecycle.returnUsedCoupon(no);
    }

    public PromotionOrderResources plan(String no) {
        var plan = orders.resources(no);
        if (plan == null
            || !Objects.equals(no, plan.orderNo())
            || plan.orderId() == null
            || plan.memberId() == null
            || plan.lines() == null
            || plan.lines().isEmpty()) throw new IllegalStateException("原订单资源快照缺失");
        return plan;
    }

    private PromotionLockBo frozen(PromotionOrderResources p) {
        return new PromotionLockBo(
            p.orderId(),
            p.orderNo(),
            p.memberId(),
            p.couponId(),
            p.activityType(),
            p.activityId(),
            p.activityRecordId(),
            p.headId(),
            p.lines().stream()
                .map(v -> new PromotionLockBo.Line(v.spuId(), v.skuId(), v.quantity()))
                .toList());
    }

    private void fence(PromotionOrderResources plan) {
        receipts.begin(
            "promotion.resource.fence.v1",
            plan.orderNo(),
            "RESOURCE_FENCE",
            plan.orderNo(),
            JsonUtils.toJsonStr(frozen(plan)));
    }

    private boolean has(String action, String no) {
        return receipts.has(
            "promotion.resource." + action + ".v1",
            "RESOURCE_" + action.toUpperCase(Locale.ROOT),
            no);
    }

    private void action(String action, PromotionOrderResources plan) {
        receipts.begin(
            "promotion.resource." + action + ".v1",
            plan.orderNo(),
            "RESOURCE_" + action.toUpperCase(Locale.ROOT),
            plan.orderNo(),
            JsonUtils.toJsonStr(frozen(plan)));
    }

    private PromotionLockDTO state(PromotionOrderResources plan) {
        String status =
            has("rollback", plan.orderNo())
                ? "RELEASED"
                : has("paid", plan.orderNo())
                ? "PAID"
                : has("lock", plan.orderNo()) ? "LOCKED" : "NOT_LOCKED";
        return new PromotionLockDTO(plan.orderNo(), status, recordId(plan));
    }

    private Long recordId(PromotionOrderResources plan) {
        if (!"COMBINATION".equals(plan.activityType())) return plan.activityRecordId();
        return combinationRecords.byUserAndActivity(plan.memberId(), plan.activityId()).stream()
            .filter(v -> Objects.equals(plan.orderId(), v.orderId()))
            .map(v -> v.id())
            .findFirst()
            .orElse(null);
    }

    private void decrease(String type, Long id, Long sku, int quantity) {
        switch (type) {
            case "SECKILL" -> seckill.decreaseStock(id, sku, quantity);
            case "POINT" -> point.decreaseStock(id, sku, quantity);
            case "BARGAIN" -> bargain.decreaseStock(id, quantity);
            case "COMBINATION" -> {
            }
            default -> throw new IllegalStateException("未知活动类型");
        }
    }

    private void restore(String type, Long id, Long sku, int quantity) {
        if (type == null) return;
        switch (type) {
            case "SECKILL" -> seckill.increaseStock(id, sku, quantity);
            case "POINT" -> point.increaseStock(id, sku, quantity);
            case "BARGAIN" -> bargain.increaseStock(id, quantity);
            case "COMBINATION" -> {
            }
            default -> throw new IllegalStateException("未知活动类型");
        }
    }
}
