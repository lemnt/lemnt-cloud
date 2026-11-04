package com.lemnt.product.infrastructure.product.gateway;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.product.client.event.StockPreReservedEvent;
import com.lemnt.product.domain.product.gateway.ProductSkuGateway;
import com.lemnt.product.domain.product.gateway.ProductStockReservationGateway;
import com.lemnt.product.domain.product.gateway.StockEventGateway;
import com.lemnt.product.domain.product.model.ProductStockReservation;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

/**
 * 预占事实以RocketMQ事务消息出站，替代本地消息表。
 * 半消息先行；executeLocalTransaction在独立短事务（REQUIRES_NEW）内按“SKU行锁→预占台账”
 * 原锁顺序完成扣减与登记，本地事务提交才允许消息投递；进程在二阶段应答前崩溃时，
 * broker回查以预占台账行存在为唯一事实依据。
 * 本地事务与调用方事务解耦：调用方后续失败必须经取消预占补偿，不存在隐式级联撤销。
 * 消息体沿用CommerceEventEnvelope JSON与原中继topic，消费者无需变更。
 */
@Component
@RequiredArgsConstructor
@RocketMQTransactionListener
@Slf4j
public class StockEventGatewayImpl implements StockEventGateway, RocketMQLocalTransactionListener {
    /** 回查定位预占台账行的消息属性，与业务键同源。 */
    public static final String HEADER_RESERVATION_NO = "lemnt-reservation-no";
    public static final String HEADER_SKU_ID = "lemnt-sku-id";

    private final ObjectProvider<RocketMQTemplate> templates;
    private final ProductSkuGateway productSkuGateway;
    private final ProductStockReservationGateway stockReservationGateway;
    private final PlatformTransactionManager transactionManager;

    @Override
    public boolean publishReserved(ProductStockReservation draft) {
        var template = templates.getIfAvailable();
        if (template == null) {
            throw new IllegalStateException("RocketMQ发布配置缺失");
        }
        var message =
            MessageBuilder.withPayload(JsonUtils.toJsonStr(envelopeOf(draft)))
                .setHeader(HEADER_RESERVATION_NO, draft.reservationNo())
                .setHeader(HEADER_SKU_ID, String.valueOf(draft.productId()))
                .build();
        var result =
            template.sendMessageInTransaction(
                CommerceEventTopics.STOCK_PRE_RESERVED, message, draft);
        return result != null
            && result.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE;
    }

    private static CommerceEventEnvelope envelopeOf(ProductStockReservation draft) {
        return new CommerceEventEnvelope(
            UUID.randomUUID().toString(),
            CommerceEventTopics.STOCK_PRE_RESERVED,
            1,
            draft.reservationNo() + ":" + draft.productId(),
            draft.reservationNo(),
            Instant.now(),
            MDC.get("traceId"),
            JsonUtils.toJsonStr(
                new StockPreReservedEvent(
                    draft.reservationNo(), draft.productId(), draft.quantity())));
    }

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object argument) {
        if (!(argument instanceof ProductStockReservation draft)) {
            log.error("预占事务消息缺少本地事务草案，直接回滚半消息");
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        Boolean reserved = newTransactionTemplate().execute(status -> reserveLocally(draft));
        return Boolean.TRUE.equals(reserved)
            ? RocketMQLocalTransactionState.COMMIT
            : RocketMQLocalTransactionState.ROLLBACK;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        var reservationNo = message.getHeaders().get(HEADER_RESERVATION_NO, String.class);
        var skuId = message.getHeaders().get(HEADER_SKU_ID, String.class);
        if (reservationNo == null || reservationNo.isBlank() || skuId == null) {
            log.warn("事务消息回查缺少台账定位属性");
            return RocketMQLocalTransactionState.UNKNOWN;
        }
        long productId;
        try {
            productId = Long.parseLong(skuId);
        } catch (NumberFormatException malformed) {
            return RocketMQLocalTransactionState.UNKNOWN;
        }
        try {
            return stockReservationGateway.find(reservationNo, productId) == null
                ? RocketMQLocalTransactionState.ROLLBACK
                : RocketMQLocalTransactionState.COMMIT;
        } catch (Exception failure) {
            log.error(
                "预占台账回查失败，交由broker稍后再次回查, reservationNo={}, skuId={}",
                reservationNo,
                productId,
                failure);
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }

    /**
     * 本地短事务：与改造前reserveStock相同的锁顺序与判定。
     * 并发对手先行（锁定行已存在）返回false并回滚；SKU不存在抛出异常并回滚，
     * 半消息经broker回查台账不存在后一并丢弃，异常向调用方传播。
     */
    private boolean reserveLocally(ProductStockReservation draft) {
        if (productSkuGateway.lockById(draft.productId()) == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (stockReservationGateway.lock(draft.reservationNo(), draft.productId()) != null) {
            return false;
        }
        if (!productSkuGateway.reserveQuantity(draft.productId(), draft.quantity())) {
            return false;
        }
        stockReservationGateway.insert(draft);
        return true;
    }

    private TransactionTemplate newTransactionTemplate() {
        var template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }
}
