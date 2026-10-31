package com.lemnt.mall.application.settlement.service;

import com.lemnt.mall.application.member.service.MemberValidationService;
import com.lemnt.mall.application.promotion.service.ActivityPricingService;
import com.lemnt.mall.application.settlement.cmd.SettlementDirectCmd;
import com.lemnt.mall.application.settlement.cmd.SettlementOrderSubmitCmd;
import com.lemnt.mall.application.settlement.dto.SettlementPreparationResult;
import com.lemnt.mall.application.settlement.qry.SettlementQuoteQry;
import com.lemnt.mall.domain.cart.gateway.CartGateway;
import com.lemnt.mall.domain.cart.model.CartLine;
import com.lemnt.mall.domain.cart.model.CartReference;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.coupon.model.Coupon;
import com.lemnt.mall.domain.member.gateway.MemberLockGateway;
import com.lemnt.mall.domain.product.gateway.ProductCatalogGateway;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.gateway.SettlementGateway;
import com.lemnt.mall.domain.settlement.model.Settlement;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;
import com.lemnt.mall.domain.settlement.model.SettlementQuote;
import com.lemnt.mall.domain.settlement.model.SettlementRecord;
import com.lemnt.mall.domain.settlement.model.SettlementRules;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 订单下单服务。
 */
@Component
@RequiredArgsConstructor
public class SettlementPreparationService {
    private final ActivityPricingService activityPricingService;
    private final SettlementDeliveryService deliveryService;
    private final CartGateway cartGateway;
    private final MemberLockGateway memberLockGateway;
    private final SettlementRecordService settlementRecordService;
    private final CouponGateway couponGateway;
    private final CouponTemplateGateway couponTemplateGateway;
    private final OrderGateway orderPersistenceService;
    private final SettlementGateway submissionCache;
    private final ProductCatalogGateway productCatalogGateway;
    private final MemberValidationService memberValidationService;
    private List<Long> selectedIds(List<Long> ids) {
        if (ids == null
                || ids.isEmpty()
                || ids.size() > 100
                || ids.stream().anyMatch(id -> id == null || id <= 0)
                || ids.stream().distinct().count() != ids.size()
        ) {
            throw new IllegalArgumentException("结算SKU编号不合法或重复");
        }
        return ids.stream().sorted().toList();
    }

    private List<CartLine> selectedCart(Long memberId, List<Long> ids) {
        var rows =
                cartGateway.cart(memberId).stream()
                .filter(row -> ids.contains(row.productId()))
                .toList();
        if (rows.size() != ids.size() || rows.stream().anyMatch(row -> !row.selected())
        ) {
            throw new IllegalArgumentException("购物车商品不属于该会员或未选中");
        }
        return rows;
    }

    private SettlementQuote price(
            Long memberId, Map<Long, Integer> quantities, Long couponId, boolean lockCoupon
    ) {
        return price(memberId, quantities, couponId, lockCoupon, false);
    }

    private SettlementQuote price(Long memberId, Map<Long, Integer> quantities, Long couponId,
            boolean lockCoupon, boolean pointStatus
    ) {
        return price(memberId, quantities, couponId, lockCoupon, pointStatus, null);
    }

    private SettlementQuote price(Long memberId, Map<Long, Integer> quantities, Long couponId,
            boolean lockCoupon, boolean pointStatus, Long addressId
    ) {
        var snapshots = productCatalogGateway.findOnShelfBatch(new ArrayList<>(quantities.keySet()));
        List<SettlementItem> items = new ArrayList<>();
        for (var entry : quantities.entrySet()
        ) {
            var product = snapshots.get(entry.getKey());
            if (product == null || product.stock() == null || product.stock() < entry.getValue()
            ) {
                throw new IllegalArgumentException("商品已下架或库存不足：" + entry.getKey());
            }
            var unit = SettlementRules.money(product.price(), false);
            items.add(
                    new SettlementItem(product.id(),
                product.productCode(),
                product.name(),
                unit,
                entry.getValue(),
                unit.multiply(BigDecimal.valueOf(entry.getValue())),
                null,
                java.math.BigDecimal.ZERO.setScale(2),
                product.spuId(),
                product.spuName(),
                product.properties() == null ? null : product.properties().stream().map(snapshotProperty ->
                        new SettlementItem.Property(snapshotProperty.propertyId(), snapshotProperty.propertyName(),
                                snapshotProperty.valueId(), snapshotProperty.valueName())).toList(),
                product.picUrl()));
        }
        Coupon coupon = null;
        if (couponId != null
        ) {
            SettlementRules.id(couponId);
            coupon = couponGateway.coupon(memberId, couponId, lockCoupon);
            if (coupon == null
            ) {
                throw new IllegalArgumentException("优惠券不属于该会员");
            }

        }
        var member = memberValidationService.requireActive(memberId);
        var quote = activityPricingService.price(items, coupon, couponTemplateGateway.now(),
                member.discountPercent(), pointStatus, member.points());
        return addressId == null ? quote : deliveryService.attach(memberId, addressId, snapshots, quote);
    }

    private SettlementOrder existing(Long memberId, String requestNo, String fingerprint) {
        var previous = settlementRecordService.findRequest(memberId, requestNo);
        if (previous == null
        ) {
            return null;
        }
        if (!previous.fingerprint().equals(fingerprint)
        ) {
            throw new IllegalStateException("结算请求编号已用于不同的请求内容");
        }
        var order = orderPersistenceService.findByOrderNo(previous.orderNo());
        if (order == null
        ) {
            order = submissionCache.submission(previous.orderNo()) == null ? null : submissionCache.submission(previous.orderNo()).order();
            if (order == null
            ) {
                throw new IllegalStateException("原提交快照已失效，请查询交易订单");
            }
        }
        return order;
    }

    private SettlementOrder persist(
            Long memberId,
            String requestNo,
            String fingerprint,
            SettlementQuote quote,
            List<CartReference> references
    ) {
        return persist(memberId, requestNo, fingerprint, quote, false, references);
    }

    private SettlementOrder persist(
            Long memberId,
            String requestNo,
            String fingerprint,
            SettlementQuote quote,
            boolean allowZero,
            List<CartReference> references
    ) {
        if (!allowZero && quote.payableAmount().signum() <= 0
        ) {
            throw new IllegalArgumentException("当前暂不支持0元结算，请更换优惠券");
        }
        String orderNo =
                "TR"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 24)
                .toUpperCase(Locale.ROOT);
        if (quote.couponId() != null
        ) {
            couponGateway.lockCoupon(memberId, quote.couponId(), orderNo);
        }
        var order =
                new SettlementOrder(
                null,
                orderNo,
                memberId,
                quote.payableAmount(),
                "RESERVING",
                quote.items(),
                null, quote.usePoint(), quote.givePoint(), quote.delivery());
        settlementRecordService.save(
                new SettlementRecord(
                orderNo,
                memberId,
                requestNo,
                fingerprint,
                quote.originalAmount(),
                quote.discountAmount(),
                quote.payableAmount(),
                quote.couponId(),
                references), order);
        return order;
    }

    /**
     * 活动订单准备：会员锁内复用请求编号，报价由调用方按活动规则生成。
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementPreparationResult preparePromotion(Long memberId, String requestNo, String fingerprint,
            Supplier<SettlementQuote> quoteSupplier, List<CartReference> references) {
        SettlementRules.id(memberId);
        String key = SettlementRules.text(requestNo, 64);
        memberLockGateway.lockMember(memberId);
        var previous = existing(memberId, key, fingerprint);
        if (previous != null) {
            return new SettlementPreparationResult(previous, false);
        }
        memberValidationService.requireActive(memberId);
        var quote = quoteSupplier.get();
        return new SettlementPreparationResult(
                persist(memberId, key, fingerprint, quote, true, references), true);
    }

    /**
     * 结算购物车。
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementOrder checkoutCart(SettlementOrderSubmitCmd cmd) {        if (cmd == null
        ) {
            throw new IllegalArgumentException("结算参数不能为空");
        }
        SettlementRules.id(cmd.memberId());
        String requestNo = SettlementRules.text(cmd.requestNo(), 64);
        var ids = selectedIds(cmd.productIds());
        if (cmd.quoteToken() == null || !cmd.quoteToken().matches("[a-f0-9]{64}")
        ) {
            throw new IllegalArgumentException("结算报价令牌不合法");
        }
        String fingerprint =
                SettlementRules.fingerprint(
                "CART:" + ids + ":" + cmd.couponId() + ":" + cmd.quoteToken());
        memberLockGateway.lockMember(cmd.memberId());
        var previous = existing(cmd.memberId(), requestNo, fingerprint);
        if (previous != null
        ) {
            return previous;
        }
        memberValidationService.requireActive(cmd.memberId());
        var rows = selectedCart(cmd.memberId(), ids);
        if (rows.stream().anyMatch(row -> cartInCheckout(row.id()))
        ) {
            throw new IllegalStateException("购物车条目正在结算，请查询已有订单");
        }
        var quantities = new TreeMap<Long, Integer>();
        rows.forEach(row -> quantities.put(row.productId(), row.quantity()));
        var quote = price(cmd.memberId(), quantities, cmd.couponId(), true);
        if (!SettlementRules.quoteToken(quote).equals(cmd.quoteToken())
        ) {
            throw new IllegalStateException("价格或数量已变化，请重新预览并确认");
        }
        return persist(
                cmd.memberId(),
                requestNo,
                fingerprint,
                quote,
                rows.stream().map(row -> new CartReference(row.id(), row.version())).toList());
    }

    /**
     * 报价。
     */
    public SettlementQuote quote(SettlementQuoteQry query) {
        if (query == null
        ) {
            throw new IllegalArgumentException("结算参数不能为空");
        }
        memberValidationService.requireActive(query.memberId());
        var ids = selectedIds(query.productIds());
        var quantities = new TreeMap<Long, Integer>();
        selectedCart(query.memberId(), ids)
                .forEach(row -> quantities.put(row.productId(), row.quantity()));
        return price(query.memberId(), quantities, query.couponId(), false, Boolean.TRUE.equals(query.pointStatus()), query.addressId());
    }

    /**
     * 创建直接。
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementOrder createDirect(SettlementDirectCmd cmd) {
        if (cmd == null || cmd.items() == null || cmd.items().isEmpty() || cmd.items().size() > 100
        ) {
            throw new IllegalArgumentException("订单必须包含1至100项商品");
        }
        SettlementRules.id(cmd.memberId());
        var quantities = new TreeMap<Long, Integer>();
        for (var line : cmd.items()
        ) {
            if (line == null
                    || line.quantity() == null
                    || line.quantity() < 1
                    || line.quantity() > 999
            ) {
                throw new IllegalArgumentException("购买数量不合法");
            }
            SettlementRules.id(line.productId());
            quantities.merge(line.productId(), line.quantity(), Integer::sum);
        }
        if (quantities.values().stream().anyMatch(q -> q > 999)
        ) {
            throw new IllegalArgumentException("同一SKU数量不能超过999");
        }
        String key =
                cmd.requestNo() == null
                ? "legacy-" + UUID.randomUUID()
                : SettlementRules.text(cmd.requestNo(), 64);
        String fingerprint =
                SettlementRules.fingerprint("DIRECT:" + quantities + ":" + cmd.couponId());
        memberLockGateway.lockMember(cmd.memberId());
        var previous = existing(cmd.memberId(), key, fingerprint);
        if (previous != null
        ) {
            return previous;
        }
        memberValidationService.requireActive(cmd.memberId());
        return persist(
                cmd.memberId(),
                key,
                fingerprint,
                price(cmd.memberId(), quantities, cmd.couponId(), true),
                List.of());
    }

    /**
     * 准备结算。
     */
    @Transactional(rollbackFor = Exception.class)
    public Settlement prepareSettlement(
            SettlementQuoteQry query
    ) {
        memberValidationService.requireActive(query.memberId());
        memberLockGateway.lockMember(query.memberId());
        var ids = selectedIds(query.productIds());
        var rows = selectedCart(query.memberId(), ids);
        var quantities = new TreeMap<Long, Integer>();
        rows.forEach(row -> quantities.put(row.productId(), row.quantity()));
        var quote = price(query.memberId(), quantities, query.couponId(), false,
            Boolean.TRUE.equals(query.pointStatus()), query.addressId());
        return new Settlement(
                UUID.randomUUID().toString(),
                query.memberId(),
                quote,
                rows.stream().map(row -> new CartReference(row.id(), row.version())).toList(),
                Instant.now().plusSeconds(900));
    }

    /**
     * 提交结算。
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementOrder submitSettlement(
            Long memberId,
            String settlementId,
            String requestNo,
            SettlementGateway settlements
    ) {
        SettlementRules.id(memberId);
        String key = SettlementRules.text(requestNo, 64);
        String id = SettlementRules.text(settlementId, 36);
        if (!id.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")
        ) {
            throw new IllegalArgumentException("结算单编号不合法");
        }
        memberLockGateway.lockMember(memberId);
        String fingerprint = SettlementRules.fingerprint("SETTLEMENT:" + id);
        var previous = existing(memberId, key, fingerprint);
        if (previous != null
        ) {
            return previous;
        }
        String used = submissionCache.orderNo(memberId, id);
        if (used != null
        ) {
            throw new IllegalStateException("结算单已用于其他提单请求，请查询原订单");
        }
        if (submissionCache.abandoned(memberId, id)
        ) {
            throw new IllegalStateException("结算提单已终止，请重新结算");
        }
        memberValidationService.requireActive(memberId);
        var snapshot = settlements.cached(id);
        if (snapshot == null
                || !memberId.equals(snapshot.memberId())
                || !id.equals(snapshot.id())
                || !snapshot.expiresAt().isAfter(Instant.now())
        ) {
            throw new IllegalArgumentException("结算单不存在或已失效，请重新结算");
        }
        if (snapshot.references().stream().anyMatch(ref -> cartInCheckout(ref.cartId()))
        ) {
            throw new IllegalStateException("商品正在结算");
        }
        if (snapshot.references().stream().anyMatch(this::cartVersionConsumed)
        ) {
            throw new IllegalStateException("购物车版本已用于未关闭订单，请查询原订单或重新结算");
        }
        var quote = snapshot.quote();
        var quantities = new TreeMap<Long, Integer>();
        quote.items().forEach(item -> quantities.put(item.productId(), item.quantity()));
        var current = price(memberId, quantities, quote.couponId(), true, quote.usePoint() > 0,
                quote.delivery() == null ? null : quote.delivery().addressId());
        if (!SettlementRules.quoteToken(current).equals(SettlementRules.quoteToken(quote))
        ) {
            throw new com.lemnt.common.core.exception.ServiceException("商品、优惠、地址或运费已变化，请重新结算", 409);
        }
        var order = persist(memberId, key, fingerprint, quote, snapshot.references());
        submissionCache.bind(memberId, id, order.orderNo());
        return order;
    }

    /**
     * 放弃结算。
     */
    @Transactional(rollbackFor = Exception.class)
    public SettlementOrder abandonSettlement(
            Long memberId,
            String settlementId,
            String requestNo,
            SettlementGateway settlements
    ) {
        SettlementRules.id(memberId);
        String key = SettlementRules.text(requestNo, 64);
        String id = SettlementRules.text(settlementId, 36);
        if (!id.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")
        ) {
            throw new IllegalArgumentException("结算单编号不合法");
        }
        memberLockGateway.lockMember(memberId);
        memberValidationService.requireActive(memberId);
        var previous = existing(memberId, key, SettlementRules.fingerprint("SETTLEMENT:" + id));
        if (previous != null
        ) {
            return previous;
        }
        String used = submissionCache.orderNo(memberId, id);
        if (used != null
        ) {
            var order = orderPersistenceService.findByOrderNo(used);
            if (order == null || !memberId.equals(order.memberId())
            ) {
                throw new IllegalStateException("结算关联记录不一致");
            }
            return order;
        }
        submissionCache.abandon(memberId, id);
        return null;
    }

    /** 检查当前购物车是否仍被正在预占的订单使用。 */
    private boolean cartInCheckout(Long cartId) {
        return submissionCache.ordersForCart(cartId).stream().anyMatch(no -> {
            var order = orderPersistenceService.findByOrderNo(no);
            return order == null || "RESERVING".equals(order.status());
        });
    }

    /** 已提交的精确版本只有原订单关闭后才能再次使用。 */
    private boolean cartVersionConsumed(CartReference reference) {
        return submissionCache.ordersForCart(reference.cartId()).stream().anyMatch(no -> {
            var context = submissionCache.submission(no);
            if (context == null || !context.record().cartReferences().contains(reference)
            ) {
                return false;
            }
            var order = orderPersistenceService.findByOrderNo(no);
            return order == null || !"CLOSED".equals(order.status());
        });
    }
}
