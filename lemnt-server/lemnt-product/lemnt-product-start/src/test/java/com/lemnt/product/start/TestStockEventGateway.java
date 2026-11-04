package com.lemnt.product.start;

import com.lemnt.product.domain.product.gateway.ProductSkuGateway;
import com.lemnt.product.domain.product.gateway.ProductStockReservationGateway;
import com.lemnt.product.domain.product.gateway.StockEventGateway;
import com.lemnt.product.domain.product.model.ProductStockReservation;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * 测试替身：按StockEventGatewayImpl的本地事务契约原样执行扣减与登记，
 * 以独立短事务提交并记录已发布草案，替代真实RocketMQ二阶段投递。
 */
public final class TestStockEventGateway implements StockEventGateway {
    private final TableTestComponents components;
    private final TransactionTemplate transaction;
    private final List<ProductStockReservation> published = new ArrayList<>();

    public TestStockEventGateway(TableTestComponents components, DataSource source) {
        this.components = components;
        this.transaction =
            new TransactionTemplate(new DataSourceTransactionManager(source));
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public boolean publishReserved(ProductStockReservation draft) {
        var skus = components.get(ProductSkuGateway.class);
        var reservations = components.get(ProductStockReservationGateway.class);
        Boolean reserved =
            transaction.execute(
                status -> {
                    if (skus.lockById(draft.productId()) == null) {
                        throw new IllegalArgumentException("商品不存在");
                    }
                    if (reservations.lock(draft.reservationNo(), draft.productId()) != null) {
                        return false;
                    }
                    if (!skus.reserveQuantity(draft.productId(), draft.quantity())) {
                        return false;
                    }
                    reservations.insert(draft);
                    return true;
                });
        if (Boolean.TRUE.equals(reserved)) {
            published.add(draft);
            return true;
        }
        return false;
    }

    public List<ProductStockReservation> published() {
        return List.copyOf(published);
    }
}
