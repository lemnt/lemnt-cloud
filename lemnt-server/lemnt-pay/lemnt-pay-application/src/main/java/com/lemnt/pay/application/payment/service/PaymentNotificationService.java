package com.lemnt.pay.application.payment.service;

import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.pay.client.event.PaySuccessEvent;
import com.lemnt.pay.domain.event.gateway.MessageOutboxGateway;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/** 仅处理已验签的现金事实；支付和Outbox同事务，不写订单或权益。 */
@Component
@RequiredArgsConstructor
public class PaymentNotificationService {
    private final OrderPaymentGateway orders;
    private final TradePaymentGateway payments;
    private final MessageOutboxGateway outbox;

    @Transactional(rollbackFor = Exception.class)
    public void execute(VerifiedPaymentNotification notice) {
        if (notice == null
                || blank(notice.paymentNo())
                || blank(notice.orderNo())
                || blank(notice.channel())
                || blank(notice.channelTradeNo())
                || blank(notice.notifyId())
                || notice.amount() == null
                || notice.amount().signum() <= 0) {
            throw new IllegalArgumentException("支付通知参数不合法");
        }
        // 只读取不可变原单归属，不对远程订单加锁或在本库事务中变更订单状态。
        var order = orders.findByOrderNo(notice.orderNo());
        if (order == null || order.memberId() == null || order.totalAmount() == null)
            throw new IllegalStateException("原订单快照缺失，通知须重试核对");
        var payment = payments.lockByPaymentNo(notice.paymentNo());
        if (payment == null
                || !notice.orderNo().equals(payment.orderNo())
                || !notice.channel().equals(payment.channel())
                || payment.amount().compareTo(notice.amount()) != 0
                || payment.amount().compareTo(order.totalAmount()) != 0) {
            throw new IllegalArgumentException("支付通知与原支付金额或订单不匹配");
        }
        if ("SUCCESS".equals(payment.status())) {
            if (!notice.channelTradeNo().equals(payment.channelTradeNo()))
                throw new IllegalStateException("成功支付收到不同渠道交易编号");
        } else {
            // 渠道确实收款时，即使订单已关闭也要保存资金事实，由order对账，不能吞掉真实付款。
            if (!Set.of("WAITING", "CLOSED").contains(payment.status())
                    || !payments.markSuccess(notice)) {
                throw new IllegalStateException("支付状态已变化或渠道结果不允许完成");
            }
        }
        outbox.enqueue(
                CommerceEventTopics.PAY_SUCCESS,
                payment.paymentNo(),
                payment.orderNo(),
                JsonUtils.toJsonStr(
                        new PaySuccessEvent(
                                payment.paymentNo(),
                                payment.orderNo(),
                                order.memberId(),
                                payment.channel(),
                                payment.amount(),
                                notice.channelTradeNo())));
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
