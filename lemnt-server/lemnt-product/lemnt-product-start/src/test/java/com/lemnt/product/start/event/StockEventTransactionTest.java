package com.lemnt.product.start.event;

import com.lemnt.product.domain.product.gateway.ProductSkuGateway;
import com.lemnt.product.domain.product.gateway.ProductStockReservationGateway;
import com.lemnt.product.domain.product.model.Product;
import com.lemnt.product.domain.product.model.ProductStockReservation;
import com.lemnt.product.infrastructure.product.gateway.StockEventGatewayImpl;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 事务消息出站的本地事务判定与回查裁决单元测试。
 * 本地事务：扣减成功才提交；库存不足、并发对手先行、SKU缺失分别回滚或抛出。
 * 回查：以预占台账行的存在为唯一事实；数据库异常答UNKNOWN等待再次回查。
 */
class StockEventTransactionTest {
    private final ProductSkuGateway skus = mock(ProductSkuGateway.class);
    private final ProductStockReservationGateway reservations =
        mock(ProductStockReservationGateway.class);
    private final PlatformTransactionManager transactionManager =
        mock(PlatformTransactionManager.class);
    private final RocketMQTemplate template = mock(RocketMQTemplate.class);

    private final ProductStockReservation draft =
        new ProductStockReservation("O1", 7L, 2, "RESERVED");

    private StockEventGatewayImpl gateway;
    private Message<String> message;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        ObjectProvider<RocketMQTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(template);
        gateway = new StockEventGatewayImpl(provider, skus, reservations, transactionManager);
        message =
            MessageBuilder.withPayload("{}")
                .setHeader(StockEventGatewayImpl.HEADER_RESERVATION_NO, "O1")
                .setHeader(StockEventGatewayImpl.HEADER_SKU_ID, "7")
                .build();
    }

    @Test
    void executeLocalTransaction_whenReservedAndRegistered_shouldCommit() {
        when(skus.lockById(7L)).thenReturn(product());
        when(reservations.lock("O1", 7L)).thenReturn(null);
        when(skus.reserveQuantity(7L, 2)).thenReturn(true);

        assertEquals(RocketMQLocalTransactionState.COMMIT, gateway.executeLocalTransaction(message, draft));
        verify(reservations).insert(draft);
        verify(transactionManager).commit(any());
    }

    @Test
    void executeLocalTransaction_whenStockInsufficient_shouldRollbackWithoutLedger() {
        when(skus.lockById(7L)).thenReturn(product());
        when(reservations.lock("O1", 7L)).thenReturn(null);
        when(skus.reserveQuantity(7L, 2)).thenReturn(false);

        assertEquals(
            RocketMQLocalTransactionState.ROLLBACK, gateway.executeLocalTransaction(message, draft));
        verify(reservations, never()).insert(any());
        // 业务判定为false时本地DB事务以空内容正常提交，回滚的是半消息
        verify(transactionManager).commit(any());
    }

    @Test
    void executeLocalTransaction_whenConcurrentWinnerAlreadyRegistered_shouldRollback() {
        when(skus.lockById(7L)).thenReturn(product());
        when(reservations.lock("O1", 7L)).thenReturn(draft);

        assertEquals(
            RocketMQLocalTransactionState.ROLLBACK, gateway.executeLocalTransaction(message, draft));
        verify(skus, never()).reserveQuantity(any(), any());
    }

    @Test
    void executeLocalTransaction_whenSkuMissing_shouldPropagateAndRollback() {
        when(skus.lockById(7L)).thenReturn(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> gateway.executeLocalTransaction(message, draft));
        verify(transactionManager).rollback(any());
    }

    @Test
    void executeLocalTransaction_whenDraftAbsent_shouldRollback() {
        assertEquals(
            RocketMQLocalTransactionState.ROLLBACK, gateway.executeLocalTransaction(message, null));
    }

    @Test
    void checkLocalTransaction_whenLedgerExists_shouldCommit() {
        when(reservations.find("O1", 7L)).thenReturn(draft);

        assertEquals(RocketMQLocalTransactionState.COMMIT, gateway.checkLocalTransaction(message));
    }

    @Test
    void checkLocalTransaction_whenLedgerMissing_shouldRollback() {
        when(reservations.find("O1", 7L)).thenReturn(null);

        assertEquals(RocketMQLocalTransactionState.ROLLBACK, gateway.checkLocalTransaction(message));
    }

    @Test
    void checkLocalTransaction_whenLookupFails_shouldStayUnknown() {
        when(reservations.find("O1", 7L))
            .thenThrow(new DataAccessResourceFailureException("db down"));

        assertEquals(RocketMQLocalTransactionState.UNKNOWN, gateway.checkLocalTransaction(message));
    }

    @Test
    void checkLocalTransaction_whenLocatingHeaderMissing_shouldStayUnknown() {
        var stripped = MessageBuilder.withPayload("{}").build();

        assertEquals(RocketMQLocalTransactionState.UNKNOWN, gateway.checkLocalTransaction(stripped));
        verify(reservations, never()).find(any(), any());
    }

    @Test
    void publishReserved_whenBrokerCommits_shouldReportSuccess() {
        var result = mock(TransactionSendResult.class);
        when(result.getLocalTransactionState()).thenReturn(LocalTransactionState.COMMIT_MESSAGE);
        when(template.sendMessageInTransaction(any(), any(Message.class), any())).thenReturn(result);

        assertTrue(gateway.publishReserved(draft));
    }

    @Test
    void publishReserved_whenLocalTransactionRolledBack_shouldReportFailure() {
        var result = mock(TransactionSendResult.class);
        when(result.getLocalTransactionState()).thenReturn(LocalTransactionState.ROLLBACK_MESSAGE);
        when(template.sendMessageInTransaction(any(), any(Message.class), any())).thenReturn(result);

        assertFalse(gateway.publishReserved(draft));
    }

    @SuppressWarnings("unchecked")
    @Test
    void publishReserved_whenTemplateMissing_shouldFailFast() {
        ObjectProvider<RocketMQTemplate> empty = mock(ObjectProvider.class);
        when(empty.getIfAvailable()).thenReturn(null);
        var unavailable =
            new StockEventGatewayImpl(empty, skus, reservations, transactionManager);

        assertThrows(IllegalStateException.class, () -> unavailable.publishReserved(draft));
    }

    private static Product product() {
        return new Product(
            7L, "CODE", "商品", null, null, new BigDecimal("1.00"), 9, "ON_SHELF", null);
    }
}
