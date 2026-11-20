package com.lemnt.third.sdk.wechatpay;

import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.model.TransactionAmount;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 支付通知映射为不暴露渠道类型的交易事实。
 */
class WechatNotificationMapperTest {

    @Test
    void mapsTransactionFields() {
        Transaction transaction = new Transaction();
        transaction.setOutTradeNo("PAY1");
        transaction.setAttach("TR1");
        transaction.setTransactionId("WX-1");
        transaction.setAppid("wx-app");
        transaction.setMchid("1900000001");
        transaction.setTradeState(Transaction.TradeStateEnum.SUCCESS);
        TransactionAmount amount = new TransactionAmount();
        amount.setTotal(800);
        amount.setCurrency("CNY");
        transaction.setAmount(amount);

        var value = WechatNotificationMapper.toTransaction(transaction);

        assertEquals("PAY1", value.outTradeNo());
        assertEquals("TR1", value.orderNo());
        assertEquals("WX-1", value.transactionId());
        assertEquals("wx-app", value.appId());
        assertEquals("1900000001", value.mchId());
        assertEquals("SUCCESS", value.tradeState());
        assertEquals("CNY", value.currency());
        assertEquals(800, value.totalFen());
    }

    @Test
    void missingTransactionAndAmountStayNull() {
        assertNull(WechatNotificationMapper.toTransaction(null));

        Transaction transaction = new Transaction();
        transaction.setOutTradeNo("PAY1");

        var value = WechatNotificationMapper.toTransaction(transaction);
        assertNull(value.currency());
        assertNull(value.totalFen());
        assertNull(value.tradeState());
    }
}
