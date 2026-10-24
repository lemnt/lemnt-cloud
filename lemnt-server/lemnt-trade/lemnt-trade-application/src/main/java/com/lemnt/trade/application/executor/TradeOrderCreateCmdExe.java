package com.lemnt.trade.application.executor;

import com.lemnt.trade.application.cmd.TradeOrderCreateCmd;
import com.lemnt.trade.domain.gateway.MemberLookupGateway;
import com.lemnt.trade.domain.gateway.ProductCatalogGateway;
import com.lemnt.trade.domain.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.model.MemberSnapshot;
import com.lemnt.trade.domain.model.ProductSnapshot;
import com.lemnt.trade.domain.model.TradeOrder;
import com.lemnt.trade.domain.model.TradeOrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TradeOrderCreateCmdExe {
    private final MemberLookupGateway members;
    private final ProductCatalogGateway products;
    private final TradeOrderGateway orders;

    @Transactional(rollbackFor = Exception.class)
    public TradeOrder execute(TradeOrderCreateCmd cmd) {
        if (cmd == null || cmd.memberId() == null || cmd.memberId() <= 0) {
            throw new IllegalArgumentException("会员ID不合法");
        }
        if (cmd.items() == null || cmd.items().isEmpty()) {
            throw new IllegalArgumentException("订单商品不能为空");
        }

        MemberSnapshot member = members.findById(cmd.memberId());
        if (member == null || !Integer.valueOf(1).equals(member.status())) {
            throw new IllegalArgumentException("会员不存在或已禁用");
        }

        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (TradeOrderCreateCmd.OrderLine line : cmd.items()) {
            if (line == null || line.productId() == null || line.productId() <= 0
                    || line.quantity() == null || line.quantity() <= 0 || line.quantity() > 999) {
                throw new IllegalArgumentException("商品或购买数量不合法");
            }
            quantities.merge(line.productId(), line.quantity(), Math::addExact);
        }
        quantities.forEach((id, quantity) -> {
            if (quantity > 999) throw new IllegalArgumentException("同一商品购买数量不能超过999");
        });

        Map<Long, ProductSnapshot> snapshots = new LinkedHashMap<>();
        for (Long productId : quantities.keySet()) {
            ProductSnapshot p = products.findOnShelf(productId);
            if (p == null || p.price() == null || p.price().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("商品不存在或已下架: " + productId);
            }
            snapshots.put(productId, p);
        }

        // The same stable reservation number is used for every product in this order.
        String orderNo = "TR" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 24).toUpperCase();
        List<Long> reservedProductIds = new ArrayList<>();
        try {
            for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
                if (!products.reserveStock(orderNo, entry.getKey(), entry.getValue())) {
                    throw new IllegalStateException("库存不足或商品已下架: " + entry.getKey());
                }
                reservedProductIds.add(entry.getKey());
            }

            List<TradeOrderItem> items = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;
            for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
                ProductSnapshot p = snapshots.get(entry.getKey());
                BigDecimal unitPrice = p.price().setScale(2, RoundingMode.HALF_UP);
                BigDecimal lineAmount = unitPrice.multiply(BigDecimal.valueOf(entry.getValue()))
                        .setScale(2, RoundingMode.HALF_UP);
                items.add(new TradeOrderItem(p.id(), p.productCode(), p.name(), unitPrice,
                        entry.getValue(), lineAmount));
                total = total.add(lineAmount);
            }
            return orders.save(TradeOrder.pendingPayment(orderNo, cmd.memberId(),
                    total.setScale(2, RoundingMode.HALF_UP), items));
        } catch (RuntimeException ex) {
            for (int i = reservedProductIds.size() - 1; i >= 0; i--) {
                try {
                    products.releaseStock(orderNo, reservedProductIds.get(i));
                } catch (RuntimeException compensation) {
                    ex.addSuppressed(compensation);
                }
            }
            throw ex;
        }
    }
}
