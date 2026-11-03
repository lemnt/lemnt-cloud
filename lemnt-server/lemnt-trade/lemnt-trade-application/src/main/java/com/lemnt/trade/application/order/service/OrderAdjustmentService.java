package com.lemnt.trade.application.order.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.order.cmd.OrderAdjustAddressCmd;
import com.lemnt.trade.application.order.cmd.OrderAdjustPriceCmd;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.trade.domain.order.model.TradeOperator;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.order.model.TradeOrderItem;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.promotion.gateway.OrderPromotionGateway;
import com.lemnt.trade.domain.promotion.model.OrderGiftAdjustment;
import com.lemnt.trade.domain.promotion.service.OrderPricingRules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/** 后台订单改价与改址：只改未支付金额或未发货收件快照，并写操作日志。 */
@Component
public class OrderAdjustmentService {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000.00");

    private final TradeOrderGateway orders;
    private final TradeOrderItemGateway items;
    private final OrderPersistenceService persistence;
    private final OrderLogService logs;
    private final TradePaymentGateway payments;

    private final OrderPromotionGateway promotions;

    @Autowired
    public OrderAdjustmentService(
            TradeOrderGateway orders,
            TradeOrderItemGateway items,
            OrderPersistenceService persistence,
            OrderLogService logs,
            TradePaymentGateway payments,
            OrderPromotionGateway promotions) {
        this.orders = orders;
        this.items = items;
        this.persistence = persistence;
        this.logs = logs;
        this.payments = payments;
        this.promotions = promotions;
    }

    public OrderAdjustmentService(
            TradeOrderGateway orders,
            TradeOrderItemGateway items,
            OrderPersistenceService persistence,
            OrderLogService logs,
            TradePaymentGateway payments) {
        this(orders, items, persistence, logs, payments, null);
    }

    /** 待支付订单按新应付金额等比例重分摊到商品行，保持行实付合计等于订单金额。 */
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder adjustPrice(OrderAdjustPriceCmd cmd) {
        validate(cmd);
        var order = lock(cmd.orderNo());
        if (!"PENDING_PAYMENT".equals(order.status())) {
            throw new ServiceException("只有待支付订单可以调价", 409);
        }
        // 支付创建也持有订单行锁；已发起的渠道金额不能通过后台调价改写。
        if (!payments.findByOrderNo(order.orderNo()).isEmpty()) {
            throw new ServiceException("订单已生成支付单，不能调价，请取消订单后重新下单", 409);
        }
        var lines = items.items(order.id());
        BigDecimal current =
                lines.stream()
                        .map(TradeOrderItem::payAmount)
                        .filter(value -> value != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (lines.isEmpty() || current.signum() <= 0) {
            throw new ServiceException("订单缺少可分摊的实付金额", 409);
        }
        var allocations = allocate(lines, current, cmd.totalAmount());
        if (!orders.updateTotalAmount(order.orderNo(), order.totalAmount(), cmd.totalAmount())) {
            throw new ServiceException("订单金额已变化，请刷新后重试", 409);
        }
        for (int index = 0; index < lines.size(); index++) {
            var line = lines.get(index);
            if (!items.updatePayAmount(order.id(), line.orderItemId(), allocations.get(index))) {
                throw new IllegalStateException("订单行实付更新失败");
            }
        }
        if ("COMPLETE".equals(order.pricingSnapshotStatus())) {
            repriceGift(order, lines, allocations, cmd.totalAmount());
        }
        var updated = persistence.findByOrderNo(order.orderNo());
        OrderPricingRules.validate(updated);
        logs.record(
                updated,
                order.status(),
                order.status(),
                2,
                TradeOperator.administrator(cmd.operatorId()),
                "后台调价："
                        + order.totalAmount().toPlainString()
                        + " → "
                        + cmd.totalAmount().toPlainString());
        return updated;
    }

    /** 仅未发货快递单可改址，地址不重算运费，避免后台绕过结算计价。 */
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder adjustAddress(OrderAdjustAddressCmd cmd) {
        validate(cmd);
        var order = lock(cmd.orderNo());
        if (!Set.of("PENDING_PAYMENT", "PAID").contains(order.status())) {
            throw new ServiceException("只有未发货订单可以改址", 409);
        }
        if (order.delivery() == null
                || !Integer.valueOf(1).equals(order.delivery().deliveryType())) {
            throw new ServiceException("自提订单不能修改收货地址", 409);
        }
        if (!orders.updateAddress(
                order.orderNo(),
                cmd.receiverName(),
                cmd.receiverMobile(),
                cmd.receiverAreaId(),
                cmd.receiverDetailAddress())) {
            throw new ServiceException("订单状态已变化，请刷新后重试", 409);
        }
        var updated = persistence.findByOrderNo(order.orderNo());
        logs.record(
                updated,
                order.status(),
                order.status(),
                11,
                TradeOperator.administrator(cmd.operatorId()),
                "后台修改收货地址：" + cmd.receiverName() + " " + cmd.receiverMobile());
        return updated;
    }

    private TradeOrder lock(String orderNo) {
        var order = persistence.findByOrderNoForUpdate(orderNo);
        if (order == null) {
            throw new ServiceException("订单不存在", 404);
        }
        return order;
    }

    /** 按原行实付占比分摊新金额，最后一行承担尾差。 */
    private List<BigDecimal> allocate(
            List<TradeOrderItem> lines, BigDecimal current, BigDecimal target) {
        var result = new ArrayList<BigDecimal>(lines.size());
        BigDecimal allocated = BigDecimal.ZERO;
        for (int index = 0; index < lines.size(); index++) {
            BigDecimal value;
            if (index == lines.size() - 1) {
                value = target.subtract(allocated).setScale(2, RoundingMode.UNNECESSARY);
            } else {
                BigDecimal pay =
                        lines.get(index).payAmount() == null
                                ? BigDecimal.ZERO
                                : lines.get(index).payAmount();
                value = pay.multiply(target).divide(current, 2, RoundingMode.DOWN);
                allocated = allocated.add(value);
            }
            result.add(value);
        }
        return result;
    }

    private void validate(OrderAdjustPriceCmd cmd) {
        if (cmd == null
                || cmd.orderNo() == null
                || !cmd.orderNo().matches("[A-Za-z0-9_-]{1,40}")
                || cmd.operatorId() == null
                || cmd.operatorId() <= 0
                || cmd.totalAmount() == null
                || cmd.totalAmount().signum() <= 0
                || cmd.totalAmount().scale() > 2
                || cmd.totalAmount().compareTo(MAX_AMOUNT) > 0) {
            throw new ServiceException("调价参数不合法", 400);
        }
    }

    private void validate(OrderAdjustAddressCmd cmd) {
        if (cmd == null
                || cmd.orderNo() == null
                || !cmd.orderNo().matches("[A-Za-z0-9_-]{1,40}")
                || cmd.operatorId() == null
                || cmd.operatorId() <= 0
                || cmd.receiverName() == null
                || cmd.receiverName().isBlank()
                || cmd.receiverName().length() > 100
                || cmd.receiverMobile() == null
                || !cmd.receiverMobile().matches("[+0-9() -]{5,32}")
                || cmd.receiverAreaId() == null
                || cmd.receiverAreaId() <= 0
                || cmd.receiverDetailAddress() == null
                || cmd.receiverDetailAddress().isBlank()
                || cmd.receiverDetailAddress().length() > 500) {
            throw new ServiceException("改址参数不合法", 400);
        }
    }

    /** 只使用建单时保存的支付赠积分系数，固定满减赠礼不随调价改变。 */
    private void repriceGift(
            TradeOrder order, List<TradeOrderItem> lines, List<BigDecimal> paid, BigDecimal total) {
        if (promotions == null) {
            throw new IllegalStateException("赠积分快照服务未配置");
        }
        var normal =
                promotions.find(order.id()).stream()
                        .filter(p -> "ORDER_GIFT".equals(p.promotionType()))
                        .findFirst();
        if (normal.isEmpty()) {
            return;
        }
        var source = normal.get();
        var matcher =
                Pattern.compile("\"givePerYuan\"\\s*:\\s*(\\d+)")
                        .matcher(source.ruleSnapshot());
        if (!matcher.find()) {
            throw new IllegalStateException("原赠积分系数缺失");
        }
        int rate = Integer.parseInt(matcher.group(1));
        long target =
                total.multiply(BigDecimal.valueOf(rate))
                        .setScale(0, RoundingMode.DOWN)
                        .longValueExact();
        var itemPoints = new LinkedHashMap<Long, Long>();
        var allocationPoints = new LinkedHashMap<Long, Long>();
        var cumulative = BigDecimal.ZERO;
        long allocated = 0;
        for (int index = 0; index < lines.size(); index++) {
            var line = lines.get(index);
            cumulative = cumulative.add(paid.get(index));
            long next =
                    total.signum() == 0
                            ? 0
                            : BigDecimal.valueOf(target)
                                    .multiply(cumulative)
                                    .divide(total, 0, RoundingMode.DOWN)
                                    .longValueExact();
            var old =
                    source.items().stream()
                            .filter(a -> line.orderItemId().equals(a.orderItemId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("原赠积分明细缺失"));
            itemPoints.put(
                    line.orderItemId(),
                    Math.addExact(line.givePoint() - old.givePoint(), next - allocated));
            allocationPoints.put(old.id(), next - allocated);
            allocated = next;
        }
        promotions.updateGift(
                new OrderGiftAdjustment(
                        order.id(),
                        source.id(),
                        Math.addExact(order.givePoint() - source.givePoint(), target),
                        target,
                        itemPoints,
                        allocationPoints));
    }
}
