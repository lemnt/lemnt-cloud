package com.lemnt.trade.application.pickup.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.order.service.OrderLogService;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.application.pickup.cmd.PickUpAssignCmd;
import com.lemnt.trade.application.pickup.cmd.PickUpVerifyCmd;
import com.lemnt.trade.application.pickup.dto.PickUpOrderDTO;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.delivery.gateway.PickUpStoreGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.TradeOperator;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;

/** 自提订单：为已支付订单分配门店和核销码，店员按码核销。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PickUpOrderService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final OrderPersistenceService orders;
    private final TradeOrderGateway orderGateway;
    private final PickUpStoreGateway stores;
    private final AfterSaleGateway afterSales;
    private final OrderLogService logs;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    /** 已支付、无处理中售后的订单才能设置为自提。 */
    public PickUpOrderDTO assign(PickUpAssignCmd cmd) {
        require(
                cmd != null
                        && cmd.orderNo() != null
                        && !cmd.orderNo().isBlank()
                        && cmd.storeId() != null
                        && cmd.storeId() > 0,
                "自提参数不合法");
        var order = orders.findByOrderNoForUpdate(cmd.orderNo());
        require(order != null, "订单不存在");
        require("PAID".equals(order.status()), "只有已支付且未发货订单可以设置为自提");
        require(!afterSales.activeRefund(order.orderNo()), "订单有处理中售后，不能设置为自提");
        var store = stores.find(cmd.storeId());
        require(store != null && Integer.valueOf(0).equals(store.status()), "自提门店不存在或已关闭");
        String code = code();
        require(orderGateway.assignPickUp(order.orderNo(), store.id(), code), "订单状态已变化，请重试");
        logs.record(
                order,
                "PAID",
                "PAID",
                45,
                TradeOperator.administrator(cmd.operatorId()),
                "设置为自提，门店：" + store.name());
        return dto(orderGateway.findHeaderByPickUpCode(code), store);
    }

    public PickUpOrderDTO preview(String verifyCode) {
        require(verifyCode != null && verifyCode.matches("\\d{8}"), "核销码格式不合法");
        var order = orderGateway.findHeaderByPickUpCode(verifyCode);
        require(order != null, "核销码不存在");
        return dto(order, stores.find(order.delivery().pickUpStoreId()));
    }

    /** 只有门店绑定的店员可以核销，核销后订单完成收货。 */
    public PickUpOrderDTO verify(PickUpVerifyCmd cmd) {
        require(
                cmd != null
                        && cmd.verifyCode() != null
                        && cmd.verifyCode().matches("\\d{8}")
                        && cmd.operatorId() != null
                        && cmd.operatorId() > 0,
                "核销参数不合法");
        var found = orderGateway.findHeaderByPickUpCode(cmd.verifyCode());
        require(found != null, "核销码不存在");
        var order = orders.findByOrderNoForUpdate(found.orderNo());
        require(order != null && "PAID".equals(order.status()), "订单状态不允许核销");
        require(
                order.delivery() != null
                        && cmd.verifyCode().equals(order.delivery().pickUpVerifyCode()),
                "核销码不存在");
        var store = stores.find(order.delivery().pickUpStoreId());
        require(store != null && Integer.valueOf(0).equals(store.status()), "自提门店不存在或已关闭");
        require(store.verifyUserIds().contains(cmd.operatorId()), "当前账号不是该门店核销店员");
        require(!afterSales.activeRefund(order.orderNo()), "订单有处理中售后，不能核销");
        var now = orderGateway.databaseNow();
        require(orderGateway.completePickUp(order.orderNo(), now), "订单状态已变化，请重试");
        logs.record(
                order,
                "PAID",
                "COMPLETED",
                46,
                TradeOperator.administrator(cmd.operatorId()),
                "自提核销完成");
        return dto(orderGateway.findHeaderByPickUpCode(cmd.verifyCode()), store);
    }

    private String code() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String value = String.format("%08d", RANDOM.nextInt(100_000_000));
            if (orderGateway.findHeaderByPickUpCode(value) == null) {
                return value;
            }
        }
        throw new IllegalStateException("核销码生成失败，请重试");
    }

    private PickUpOrderDTO dto(
            com.lemnt.trade.domain.order.model.TradeOrder order,
            com.lemnt.trade.domain.delivery.model.PickUpStore store) {
        var delivery = order.delivery();
        return new PickUpOrderDTO(
                order.orderNo(),
                order.status(),
                store == null ? null : store.id(),
                store == null ? null : store.name(),
                store == null ? null : store.phone(),
                store == null ? null : store.detailAddress(),
                delivery == null ? null : delivery.pickUpVerifyCode());
    }
}
