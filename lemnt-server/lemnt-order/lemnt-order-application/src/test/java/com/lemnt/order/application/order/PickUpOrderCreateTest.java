package com.lemnt.order.application.order;

import com.lemnt.order.application.dubbo.TradeOrderClientImpl;
import com.lemnt.order.application.order.service.OrderCommentService;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.application.order.service.OrderRemoteService;
import com.lemnt.order.application.order.service.OrderSubmissionService;
import com.lemnt.order.client.bo.OrderCreateBo;
import com.lemnt.order.client.dto.OrderDeliveryDTO;
import com.lemnt.order.client.dto.OrderItemDTO;
import com.lemnt.order.domain.config.gateway.TradeConfigGateway;
import com.lemnt.order.domain.config.model.TradeConfig;
import com.lemnt.order.domain.delivery.gateway.PickUpStoreGateway;
import com.lemnt.order.domain.delivery.model.PickUpStore;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.model.TradeOrder;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** 自提订单在交易建单时生成核销码，关闭自提或门店不可用直接拒绝。 */
class PickUpOrderCreateTest {
    final OrderSubmissionService submissions = mock(OrderSubmissionService.class);
    final OrderPersistenceService persistence = mock(OrderPersistenceService.class);
    final OrderRemoteService remotes = mock(OrderRemoteService.class);
    final OrderCommentService comments = mock(OrderCommentService.class);
    final TradeOrderGateway orderGateway = mock(TradeOrderGateway.class);
    final TradeConfigGateway configs = mock(TradeConfigGateway.class);
    final PickUpStoreGateway stores = mock(PickUpStoreGateway.class);
    final TradeOrderClientImpl client =
            new TradeOrderClientImpl(
                    submissions, persistence, remotes, comments, orderGateway, configs, stores);

    TradeConfig config(boolean enabled) {
        return new TradeConfig(
                1L, List.of(), List.of(), false, 0, enabled, false, 1, 1, List.of(), 0, 0, 0, 0, 0,
                List.of());
    }

    PickUpStore store(int status) {
        return new PickUpStore(
                9L,
                "中心店",
                null,
                "13800000000",
                1L,
                "中心路1号",
                null,
                "00:00:00",
                "23:59:59",
                null,
                null,
                List.of(1L),
                status);
    }

    OrderCreateBo order() {
        var item =
                new OrderItemDTO(
                        5L,
                        "P1",
                        "商品",
                        new BigDecimal("10.00"),
                        1,
                        new BigDecimal("10.00"),
                        new BigDecimal("10.00"),
                        BigDecimal.ZERO,
                        1L,
                        "商品",
                        List.of(),
                        null);
        return new OrderCreateBo(
                "TR1",
                7L,
                new BigDecimal("10.00"),
                List.of(item),
                0,
                0,
                new OrderDeliveryDTO(null, 2, null, null, null, null, new BigDecimal("0.00"), 9L));
    }

    @Test
    void pickupOrderGeneratesEightDigitCode() {
        when(configs.get()).thenReturn(config(true));
        when(stores.find(9L)).thenReturn(store(0));
        when(orderGateway.findHeaderByPickUpCode(anyString())).thenReturn(null);
        when(persistence.findByOrderNo("TR1"))
                .thenReturn(
                        new TradeOrder(
                                1L,
                                "TR1",
                                7L,
                                new BigDecimal("10.00"),
                                "RESERVING",
                                List.of(),
                                null));

        client.create(order());

        var captor = ArgumentCaptor.forClass(TradeOrder.class);
        verify(submissions).create(captor.capture());
        assertEquals(2, captor.getValue().delivery().deliveryType());
        assertEquals(9L, captor.getValue().delivery().pickUpStoreId());
        assertTrue(captor.getValue().delivery().pickUpVerifyCode().matches("\\d{8}"));
    }

    @Test
    void overnightAndAllDayStoresAllowOrderCreation() {
        when(configs.get()).thenReturn(config(true));
        when(persistence.findByOrderNo("TR1"))
                .thenReturn(
                        new TradeOrder(
                                1L,
                                "TR1",
                                7L,
                                new BigDecimal("10.00"),
                                "RESERVING",
                                List.of(),
                                null));
        var format = DateTimeFormatter.ofPattern("HH:mm:ss");
        var now = LocalTime.now();
        String open = now.isBefore(LocalTime.NOON) ? "23:59:59" : format.format(now.minusHours(1));
        String close = now.isBefore(LocalTime.NOON) ? format.format(now.plusHours(1)) : "00:00:00";
        when(stores.find(9L))
                .thenReturn(
                        new PickUpStore(
                                9L,
                                "跨夜店",
                                null,
                                "13800000000",
                                1L,
                                "地址",
                                null,
                                open,
                                close,
                                null,
                                null,
                                List.of(1L),
                                0));
        assertDoesNotThrow(() -> client.create(order()));
        when(stores.find(9L))
                .thenReturn(
                        new PickUpStore(
                                9L,
                                "全天店",
                                null,
                                "13800000000",
                                1L,
                                "地址",
                                null,
                                "00:00:00",
                                "00:00:00",
                                null,
                                null,
                                List.of(1L),
                                0));
        assertDoesNotThrow(() -> client.create(order()));
    }

    @Test
    void disabledPickupOrClosedStoreIsRejected() {
        when(configs.get()).thenReturn(config(false));
        assertThrows(IllegalArgumentException.class, () -> client.create(order()));
        when(configs.get()).thenReturn(config(true));
        when(stores.find(9L)).thenReturn(store(1));
        assertThrows(IllegalArgumentException.class, () -> client.create(order()));
        var later = LocalTime.now().plusHours(1).withNano(0);
        when(stores.find(9L))
                .thenReturn(
                        new PickUpStore(
                                9L,
                                "中心店",
                                null,
                                "13800000000",
                                1L,
                                "中心路1号",
                                null,
                                later.toString(),
                                later.plusHours(1).toString(),
                                null,
                                null,
                                List.of(1L),
                                0));
        assertThrows(IllegalArgumentException.class, () -> client.create(order()));
    }
}
