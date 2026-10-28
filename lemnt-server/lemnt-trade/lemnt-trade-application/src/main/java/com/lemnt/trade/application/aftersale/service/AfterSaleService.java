package com.lemnt.trade.application.aftersale.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleCreateCmd;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleRefuseCmd;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleRejectCmd;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleReturnCmd;
import com.lemnt.trade.application.aftersale.dto.AfterSaleItemDTO;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.aftersale.model.AfterSale;
import com.lemnt.trade.domain.fulfillment.model.RefundAllocation;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.trade.domain.order.model.OrderRules;
import com.lemnt.trade.domain.order.model.TradeOperator;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** 商品售后审批和退货流程，所有写入沿用订单行锁串行校验额度。 */
@Component
@RequiredArgsConstructor
public class AfterSaleService {
    private final TradeOrderGateway orders;
    private final TradeOrderItemGateway items;
    private final AfterSaleGateway afterSales;
    private final TradePaymentGateway payments;
    private final AfterSaleLogService logs;
    private static final Set<String> RELEASED = Set.of("REJECTED", "RETURN_REJECTED", "CANCELLED");

    /** 展示可售后商品行、剩余数量及金额，最终创建时仍在行锁下重新校验。 */
    public List<AfterSaleItemDTO> items(String orderNo) {
        var order = orders.findHeader(OrderRules.text(orderNo, 40));
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return quotas(order);
    }

    private List<AfterSaleItemDTO> quotas(TradeOrder order) {
        var lines = items.refundItems(order.id());
        var allocated = RefundAllocation.allocate(order.totalAmount(), lines);
        var history = afterSales.afterSales(order.orderNo()).stream()
                .filter(value -> !RELEASED.contains(value.status())).toList();
        boolean whole = history.stream().anyMatch(value -> value.orderItemId() == null);
        return lines.stream().map(line -> {
            var used = history.stream().filter(value -> line.id().equals(value.orderItemId())).toList();
            int quantity = used.stream().mapToInt(AfterSale::quantity).sum();
            var amount = used.stream().map(AfterSale::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (quantity > line.quantity() || amount.compareTo(allocated.get(line.id())) > 0) {
                throw new IllegalStateException("历史商品售后额度不一致，请先对账");
            }
            return new AfterSaleItemDTO(line.id(),
                line.productId(),
                line.productName(),
                line.quantity(),
                whole ? 0 : line.quantity() - quantity,
                whole ? BigDecimal.ZERO.setScale(2) : allocated.get(line.id()).subtract(amount),
                line.spuId(),
                line.spuName(),
                line.properties(),
                line.picUrl());
        }).toList();
    }

    /** 根据商品行剩余额度生成部分售后，金额由服务端计算，重复请求核对全部业务字段。 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale create(AfterSaleCreateCmd cmd) {
        return create(cmd, TradeOperator.system());
    }

    /** 显式记录实际发起方，会员操作同时核对归属。 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale create(AfterSaleCreateCmd cmd, TradeOperator operator) {
        if (cmd == null || cmd.orderItemId() == null || cmd.orderItemId() <= 0
                || cmd.quantity() == null || cmd.quantity() < 1 || cmd.quantity() > 999
                || cmd.type() == null || !Set.of("REFUND_ONLY", "RETURN_REFUND").contains(cmd.type())) {
            throw new IllegalArgumentException("商品售后参数不合法");
        }
        String no = OrderRules.text(cmd.orderNo(), 40);
        String key = OrderRules.text(cmd.requestNo(), 64);
        String reason = OrderRules.text(cmd.reason(), 500);
        var order = orders.lockHeader(no);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        requireOwner(order.memberId(), operator);
        var previous = afterSales.findRequest(no, key);
        if (previous != null) {
            var existing = afterSales.afterSale(previous.refundNo());
            if (!cmd.orderItemId().equals(existing.orderItemId()) || !cmd.quantity().equals(existing.quantity())
                    || !cmd.type().equals(existing.type()) || !reason.equals(existing.reason())) {
                throw new IllegalStateException("售后请求编号已用于不同内容");
            }
            return existing;
        }
        if (!Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())
                || ("RETURN_REFUND".equals(cmd.type()) && "PAID".equals(order.status()))) {
            throw new IllegalStateException("仅已支付订单可售后，退货退款须在发货后申请");
        }
        var quota = quotas(order).stream().filter(value -> cmd.orderItemId().equals(value.orderItemId()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("订单商品不存在"));
        if (cmd.quantity() > quota.refundableQuantity()) {
            throw new IllegalStateException("商品剩余可售后数量不足");
        }
        var amount = quota.refundableAmount().multiply(BigDecimal.valueOf(cmd.quantity()))
                .divide(BigDecimal.valueOf(quota.refundableQuantity()), 2, RoundingMode.DOWN);
        if (amount.signum() <= 0) {
            throw new IllegalStateException("该数量对应的退款金额不足一分，请合并申请");
        }
        var successful = payments.findByOrderNo(no).stream().filter(value -> "SUCCESS".equals(value.status())).toList();
        if (successful.size() != 1 || successful.get(0).amount().compareTo(order.totalAmount()) != 0) {
            throw new IllegalStateException("订单成功支付记录不一致，请先对账");
        }
        var value = new AfterSale("RF" + UUID.randomUUID().toString().replace("-", ""),
                no,
                key,
                order.memberId(),
                successful.get(0).paymentNo(),
                cmd.orderItemId(),
                cmd.quantity(),
                cmd.type(),
                amount,
                reason,
                "REQUESTED",
                null,
                null,
                null,
                orders.databaseNow(),
                null,
                null,
                null,
                null,
                order.id(),
                null,
                null,
                quota.productId(),
                quota.spuId(),
                quota.spuName(),
                quota.properties(),
                quota.picUrl());
        afterSales.saveAfterSale(value);
        var saved = afterSales.afterSale(value.refundNo());
        logs.record(saved, null, "REQUESTED", 10, operator, "申请商品售后");
        return saved;
    }

    /** 同意仅退款后待退款，同意退货退款后等待寄回，重复审批不倒退状态。 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale agree(String no) {
        return agree(no, TradeOperator.system());
    }

    /** 显式记录实际发起方，会员操作同时核对归属。 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale agree(String no, TradeOperator operator) {
        var value = lock(no);
        requireOwner(value.memberId(), operator);
        if (!"REQUESTED".equals(value.status())) {
            if (Set.of("AWAITING_RETURN", "RETURNED", "WAIT_REFUND", "REFUNDING", "REFUNDED")
                    .contains(value.status())) {
                return value;
            }
            throw new IllegalStateException("售后状态不允许同意");
        }
        return update(value, "RETURN_REFUND".equals(value.type()) ? "AWAITING_RETURN" : "WAIT_REFUND",
                null, orders.databaseNow(), value.returnCarrierCode(), value.returnTrackingNo(),
                value.returnShippedAt(), value.returnReceivedAt(), operator, 11, "商家同意退款");
    }

    /** 待审核售后可拒绝，释放该商品数量与金额额度。 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale reject(AfterSaleRejectCmd cmd) {
        return reject(cmd, TradeOperator.system());
    }

    /** 显式记录实际发起方，会员操作同时核对归属。 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale reject(AfterSaleRejectCmd cmd, TradeOperator operator) {
        var value = lock(cmd.afterSaleNo());
        requireOwner(value.memberId(), operator);
        String reason = OrderRules.text(cmd.reason(), 500);
        if ("REJECTED".equals(value.status()) && reason.equals(value.decisionReason())) {
            return value;
        }
        if (!"REQUESTED".equals(value.status())) {
            throw new IllegalStateException("售后状态不允许拒绝");
        }
        return update(value, "REJECTED", reason, orders.databaseNow(), null, null, null, null, operator, 12, "商家拒绝退款：" + reason);
    }

    /** 登记已同意退货的承运商与运单，已登记运单不可改写。 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale registerReturn(AfterSaleReturnCmd cmd) {
        return registerReturn(cmd, TradeOperator.system());
    }

    /** 显式记录实际发起方，会员操作同时核对归属。 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale registerReturn(AfterSaleReturnCmd cmd, TradeOperator operator) {
        var value = lock(cmd.afterSaleNo());
        requireOwner(value.memberId(), operator);
        String carrier = OrderRules.text(cmd.carrierCode(), 40);
        String tracking = OrderRules.text(cmd.trackingNo(), 100);
        if (value.returnTrackingNo() != null) {
            if (!carrier.equals(value.returnCarrierCode()) || !tracking.equals(value.returnTrackingNo())) {
                throw new IllegalStateException("退货运单已登记，不能改写");
            }
            return value;
        }
        if (!"AWAITING_RETURN".equals(value.status()) || !"RETURN_REFUND".equals(value.type())) {
            throw new IllegalStateException("售后尚未同意退货");
        }
        return update(value, "RETURNED", value.decisionReason(), value.decidedAt(), carrier, tracking,
                orders.databaseNow(), null, operator, 20, "填写退货物流信息，快递公司：" + carrier + "，快递单号：" + tracking);
    }

    /** 退货确认收到后才允许原路退款。 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale receive(String no) {
        return receive(no, TradeOperator.system());
    }

    /** 显式记录实际发起方，会员操作同时核对归属。 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale receive(String no, TradeOperator operator) {
        var value = lock(no);
        requireOwner(value.memberId(), operator);
        if (value.returnReceivedAt() != null && !"RETURN_REJECTED".equals(value.status())) {
            return value;
        }
        if (!"RETURNED".equals(value.status())) {
            throw new IllegalStateException("尚未登记退货，不能确认收到");
        }
        return update(value, "WAIT_REFUND", value.decisionReason(), value.decidedAt(),
                value.returnCarrierCode(), value.returnTrackingNo(), value.returnShippedAt(), orders.databaseNow(), operator, 21, "商家收货");
    }

    /** 拒收已寄回商品后结束本次售后，不能同时发起退款。 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale refuse(AfterSaleRefuseCmd cmd) {
        return refuse(cmd, TradeOperator.system());
    }

    /** 显式记录实际发起方，会员操作同时核对归属。 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale refuse(AfterSaleRefuseCmd cmd, TradeOperator operator) {
        var value = lock(cmd.afterSaleNo());
        requireOwner(value.memberId(), operator);
        String reason = OrderRules.text(cmd.reason(), 500);
        if ("RETURN_REJECTED".equals(value.status()) && reason.equals(value.decisionReason())) {
            return value;
        }
        if (!"RETURNED".equals(value.status())) {
            throw new IllegalStateException("售后状态不允许拒收");
        }
        return update(value, "RETURN_REJECTED", reason, value.decidedAt(), value.returnCarrierCode(),
                value.returnTrackingNo(), value.returnShippedAt(), orders.databaseNow(), operator, 22, "商家拒收货：" + reason);
    }

    private AfterSale lock(String no) {
        var initial = afterSales.afterSale(OrderRules.text(no, 40));
        if (initial == null || orders.lockHeader(initial.orderNo()) == null) {
            throw new ServiceException("售后单不存在", 400);
        }
        return afterSales.afterSale(initial.refundNo());
    }

    private AfterSale update(AfterSale value, String status, String reason, LocalDateTime decidedAt,
            String carrier, String tracking, LocalDateTime shippedAt, LocalDateTime receivedAt,
            TradeOperator operator, int operateType, String content) {
        var result = new AfterSale(value.refundNo(),
                value.orderNo(),
                value.requestNo(),
                value.memberId(),
                value.paymentNo(),
                value.orderItemId(),
                value.quantity(),
                value.type(),
                value.amount(),
                value.reason(),
                status,
                reason,
                carrier,
                tracking,
                value.requestedAt(),
                decidedAt,
                shippedAt,
                receivedAt,
                value.id(),
                value.orderId(),
                value.refundTime(),
                (operateType == 11 || operateType == 12) && operator.userType() == 2 ? operator.userId() : value.auditUserId(),
                value.skuId(),
                value.spuId(),
                value.spuName(),
                value.properties(),
                value.picUrl());
        if (!afterSales.updateAfterSale(result, value.status())) {
            throw new IllegalStateException("售后状态已变化");
        }
        logs.record(value, value.status(), status, operateType, operator, content);
        return afterSales.afterSale(result.refundNo());
    }
    /** 会员仅能取消申请、同意退货或已寄回阶段，等待退款后禁止取消。 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale cancel(Long memberId, String no) {
        var operator = TradeOperator.member(memberId);
        var value = lock(no);
        requireOwner(value.memberId(), operator);
        if ("CANCELLED".equals(value.status())) { return value; }
        if (!Set.of("REQUESTED", "AWAITING_RETURN", "RETURNED").contains(value.status())) {
            throw new ServiceException("当前售后状态不能取消", 400);
        }
        return update(value, "CANCELLED", value.decisionReason(), value.decidedAt(), value.returnCarrierCode(),
                value.returnTrackingNo(), value.returnShippedAt(), value.returnReceivedAt(), operator, 40, "会员取消售后");
    }

    /** 不向其他会员泄露售后是否存在。 */
    private void requireOwner(Long memberId, TradeOperator operator) {
        if (operator.userType() == 1 && !operator.userId().equals(memberId)) {
            throw new ServiceException("售后单不存在", 400);
        }
    }
}
