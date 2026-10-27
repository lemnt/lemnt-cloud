package com.lemnt.mall.application.checkout.service;

import com.lemnt.mall.application.checkout.cmd.CheckoutDirectCmd;
import com.lemnt.mall.application.checkout.cmd.CheckoutSubmitCmd;
import com.lemnt.mall.application.checkout.qry.CheckoutQuoteQry;
import com.lemnt.mall.application.checkout.service.CheckoutService;
import com.lemnt.mall.domain.catalog.gateway.ProductCatalogGateway;
import com.lemnt.mall.domain.checkout.gateway.CartGateway;
import com.lemnt.mall.domain.checkout.gateway.CheckoutAccountGateway;
import com.lemnt.mall.domain.checkout.gateway.CheckoutCartGateway;
import com.lemnt.mall.domain.checkout.gateway.CouponGateway;
import com.lemnt.mall.domain.checkout.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.checkout.gateway.SettlementGateway;
import com.lemnt.mall.domain.checkout.model.CartLine;
import com.lemnt.mall.domain.checkout.model.CartReference;
import com.lemnt.mall.domain.checkout.model.CheckoutItem;
import com.lemnt.mall.domain.checkout.model.CheckoutQuote;
import com.lemnt.mall.domain.checkout.model.CheckoutRecord;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;
import com.lemnt.mall.domain.checkout.model.Coupon;
import com.lemnt.mall.domain.checkout.model.CouponTemplate;
import com.lemnt.mall.domain.checkout.model.Settlement;
import com.lemnt.mall.domain.order.gateway.OrderGateway;
import com.lemnt.mall.domain.order.model.OrderSnapshot;

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

/**
 * 订单下单服务。
 */
@Component
@RequiredArgsConstructor
public class CheckoutPreparationService {
    private final CartGateway cartGateway;
    private final com.lemnt.mall.domain.checkout.gateway.SettlementOrderGateway settlementOrderGateway;
    private final com.lemnt.mall.domain.checkout.gateway.SettlementAbandonGateway settlementAbandonGateway;
    private final CheckoutAccountGateway checkoutAccountGateway;
    private final CheckoutCartGateway checkoutCartGateway;
    private final CheckoutService checkoutService;
    private final CouponGateway couponGateway;
    private final CouponTemplateGateway couponTemplateGateway;
    private final OrderGateway orderPersistenceService;
    private final com.lemnt.mall.domain.checkout.gateway.SettlementGateway submissionCache;
    private final ProductCatalogGateway productCatalogGateway;
    private final CheckoutMemberService checkoutMemberService;
    private List<Long> selectedIds(List<Long> ids) {
        if (ids == null
                || ids.isEmpty()
                || ids.size() > 100
                || ids.stream().anyMatch(id -> id == null || id <= 0)
                || ids.stream().distinct().count() != ids.size()) {
            throw new IllegalArgumentException("结算SKU编号不合法或重复");
        }
        return ids.stream().sorted().toList();
    }

    private List<CartLine> selectedCart(Long memberId, List<Long> ids) {
        var rows =
                cartGateway.cart(memberId).stream()
                .filter(row -> ids.contains(row.productId()))
                .toList();
        if (rows.size() != ids.size() || rows.stream().anyMatch(row -> !row.selected())) {
            throw new IllegalArgumentException("购物车商品不属于该会员或未选中");
        }
        return rows;
    }

    private CheckoutQuote price(
            Long memberId, Map<Long, Integer> quantities, Long couponId, boolean lockCoupon) {
        var snapshots = productCatalogGateway.findOnShelfBatch(new ArrayList<>(quantities.keySet()));
        List<CheckoutItem> items = new ArrayList<>();
        for (var entry : quantities.entrySet()) {
            var product = snapshots.get(entry.getKey());
            if (product == null || product.stock() == null || product.stock() < entry.getValue()) {
                throw new IllegalArgumentException("商品已下架或库存不足：" + entry.getKey());
            }
            var unit = CheckoutRules.money(product.price(), false);
            items.add(
                    new CheckoutItem(
                    product.id(),
                    product.productCode(),
                    product.name(),
                    unit,
                    entry.getValue(),
                    unit.multiply(BigDecimal.valueOf(entry.getValue()))));
        }
        Coupon coupon = null;
        CouponTemplate template = null;
        if (couponId != null) {
            CheckoutRules.id(couponId);
            coupon = couponGateway.coupon(memberId, couponId, lockCoupon);
            if (coupon == null) {
                throw new IllegalArgumentException("优惠券不属于该会员");
            }
            template =
                    lockCoupon
                    ? couponTemplateGateway.lockTemplate(coupon.templateId())
                    : couponTemplateGateway.template(coupon.templateId());
        }
        return CheckoutRules.quote(items, coupon, template, checkoutAccountGateway.now());
    }

    private OrderSnapshot existing(Long memberId, String requestNo, String fingerprint) {
        var previous = checkoutService.findRequest(memberId, requestNo);
        if (previous == null) {
            return null;
        }
        if (!previous.fingerprint().equals(fingerprint)) {
            throw new IllegalStateException("结算请求编号已用于不同的请求内容");
        }
        var order = orderPersistenceService.findByOrderNo(previous.orderNo());
        if (order == null) {
            order = submissionCache.cachedOrder(previous.orderNo());
            if (order == null) {
                throw new IllegalStateException("原提交快照已失效，请查询交易订单");
            }
        }
        return order;
    }

    private OrderSnapshot persist(
            Long memberId,
            String requestNo,
            String fingerprint,
            CheckoutQuote quote,
            List<CartReference> references) {
        if (quote.payableAmount().signum() <= 0) {
            throw new IllegalArgumentException("当前暂不支持0元结算，请更换优惠券");
        }
        String orderNo =
                "TR"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 24)
                .toUpperCase(Locale.ROOT);
        if (quote.couponId() != null) {
            couponGateway.lockCoupon(memberId, quote.couponId(), orderNo);
        }
        var order =
                new OrderSnapshot(
                null,
                orderNo,
                memberId,
                quote.payableAmount(),
                "RESERVING",
                quote.items(),
                null);
        submissionCache.cacheOrder(order);
        checkoutService.save(
                new CheckoutRecord(
                orderNo,
                memberId,
                requestNo,
                fingerprint,
                quote.originalAmount(),
                quote.discountAmount(),
                quote.payableAmount(),
                quote.couponId(),
                references));
        return order;
    }

    /**
     * 结算购物车。
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderSnapshot checkoutCart(CheckoutSubmitCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("结算参数不能为空");
        }
        CheckoutRules.id(cmd.memberId());
        String requestNo = CheckoutRules.text(cmd.requestNo(), 64);
        var ids = selectedIds(cmd.productIds());
        if (cmd.quoteToken() == null || !cmd.quoteToken().matches("[a-f0-9]{64}")) {
            throw new IllegalArgumentException("结算报价令牌不合法");
        }
        String fingerprint =
                CheckoutRules.fingerprint(
                "CART:" + ids + ":" + cmd.couponId() + ":" + cmd.quoteToken());
        checkoutAccountGateway.lockMember(cmd.memberId());
        var previous = existing(cmd.memberId(), requestNo, fingerprint);
        if (previous != null) {
            return previous;
        }
        checkoutMemberService.requireActive(cmd.memberId());
        var rows = selectedCart(cmd.memberId(), ids);
        if (rows.stream().anyMatch(row -> checkoutCartGateway.cartInCheckout(row.id()))) {
            throw new IllegalStateException("购物车条目正在结算，请查询已有订单");
        }
        var quantities = new TreeMap<Long, Integer>();
        rows.forEach(row -> quantities.put(row.productId(), row.quantity()));
        var quote = price(cmd.memberId(), quantities, cmd.couponId(), true);
        if (!CheckoutRules.quoteToken(quote).equals(cmd.quoteToken())) {
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
    public CheckoutQuote quote(CheckoutQuoteQry query) {
        if (query == null) {
            throw new IllegalArgumentException("结算参数不能为空");
        }
        checkoutMemberService.requireActive(query.memberId());
        var ids = selectedIds(query.productIds());
        var quantities = new TreeMap<Long, Integer>();
        selectedCart(query.memberId(), ids)
                .forEach(row -> quantities.put(row.productId(), row.quantity()));
        return price(query.memberId(), quantities, query.couponId(), false);
    }

    /**
     * 创建直接。
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderSnapshot createDirect(CheckoutDirectCmd cmd) {
        if (cmd == null || cmd.items() == null || cmd.items().isEmpty() || cmd.items().size() > 100) {
            throw new IllegalArgumentException("订单必须包含1至100项商品");
        }
        CheckoutRules.id(cmd.memberId());
        var quantities = new TreeMap<Long, Integer>();
        for (var line : cmd.items()) {
            if (line == null
                    || line.quantity() == null
                    || line.quantity() < 1
                    || line.quantity() > 999) {
                throw new IllegalArgumentException("购买数量不合法");
            }
            CheckoutRules.id(line.productId());
            quantities.merge(line.productId(), line.quantity(), Integer::sum);
        }
        if (quantities.values().stream().anyMatch(q -> q > 999)) {
            throw new IllegalArgumentException("同一SKU数量不能超过999");
        }
        String key =
                cmd.requestNo() == null
                ? "legacy-" + UUID.randomUUID()
                : CheckoutRules.text(cmd.requestNo(), 64);
        String fingerprint =
                CheckoutRules.fingerprint("DIRECT:" + quantities + ":" + cmd.couponId());
        checkoutAccountGateway.lockMember(cmd.memberId());
        var previous = existing(cmd.memberId(), key, fingerprint);
        if (previous != null) {
            return previous;
        }
        checkoutMemberService.requireActive(cmd.memberId());
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
            CheckoutQuoteQry query) {
        checkoutMemberService.requireActive(query.memberId());
        checkoutAccountGateway.lockMember(query.memberId());
        var ids = selectedIds(query.productIds());
        var rows = selectedCart(query.memberId(), ids);
        var quantities = new TreeMap<Long, Integer>();
        rows.forEach(row -> quantities.put(row.productId(), row.quantity()));
        var quote = price(query.memberId(), quantities, query.couponId(), false);
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
    public OrderSnapshot submitSettlement(
            Long memberId,
            String settlementId,
            String requestNo,
            SettlementGateway settlements) {
        CheckoutRules.id(memberId);
        String key = CheckoutRules.text(requestNo, 64);
        String id = CheckoutRules.text(settlementId, 36);
        if (!id.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")) {
            throw new IllegalArgumentException("结算单编号不合法");
        }
        checkoutAccountGateway.lockMember(memberId);
        String fingerprint = CheckoutRules.fingerprint("SETTLEMENT:" + id);
        var previous = existing(memberId, key, fingerprint);
        if (previous != null) {
            return previous;
        }
        String used = settlementOrderGateway.orderNo(memberId, id);
        if (used != null) {
            throw new IllegalStateException("结算单已用于其他提单请求，请查询原订单");
        }
        if (settlementAbandonGateway.abandoned(memberId, id)) {
            throw new IllegalStateException("结算提单已终止，请重新结算");
        }
        checkoutMemberService.requireActive(memberId);
        var snapshot = settlements.cached(id);
        if (snapshot == null
                || !memberId.equals(snapshot.memberId())
                || !id.equals(snapshot.id())
                || !snapshot.expiresAt().isAfter(Instant.now())) {
            throw new IllegalArgumentException("结算单不存在或已失效，请重新结算");
        }
        if (snapshot.references().stream().anyMatch(ref -> checkoutCartGateway.cartInCheckout(ref.cartId()))) {
            throw new IllegalStateException("商品正在结算");
        }
        if (snapshot.references().stream().anyMatch(checkoutCartGateway::cartVersionConsumed)) {
            throw new IllegalStateException("购物车版本已用于未关闭订单，请查询原订单或重新结算");
        }
        var quote = snapshot.quote();
        var quantities = new TreeMap<Long, Integer>();
        quote.items().forEach(item -> quantities.put(item.productId(), item.quantity()));
        var current = price(memberId, quantities, quote.couponId(), true);
        if (!CheckoutRules.quoteToken(current).equals(CheckoutRules.quoteToken(quote))) {
            throw new IllegalStateException("商品或优惠条件已变化，请重新结算");
        }
        var order = persist(memberId, key, fingerprint, quote, snapshot.references());
        settlementOrderGateway.bind(memberId, id, order.orderNo());
        return order;
    }

    /**
     * 放弃结算。
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderSnapshot abandonSettlement(
            Long memberId,
            String settlementId,
            String requestNo,
            SettlementGateway settlements) {
        CheckoutRules.id(memberId);
        String key = CheckoutRules.text(requestNo, 64);
        String id = CheckoutRules.text(settlementId, 36);
        if (!id.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")) {
            throw new IllegalArgumentException("结算单编号不合法");
        }
        checkoutAccountGateway.lockMember(memberId);
        checkoutMemberService.requireActive(memberId);
        var previous = existing(memberId, key, CheckoutRules.fingerprint("SETTLEMENT:" + id));
        if (previous != null) {
            return previous;
        }
        String used = settlementOrderGateway.orderNo(memberId, id);
        if (used != null) {
            var order = orderPersistenceService.findByOrderNo(used);
            if (order == null || !memberId.equals(order.memberId())) {
                throw new IllegalStateException("结算关联记录不一致");
            }
            return order;
        }
        settlementAbandonGateway.abandon(memberId, id);
        return null;
    }
}
