package com.lemnt.third.sdk.wechatpay;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 预支付与退款入参在 SDK 入口即校验，避免发出无效请求。
 */
class WechatPrepayCommandTest {

    @Test
    void prepayCommandAcceptsBusinessValues() {
        assertDoesNotThrow(
            () ->
                new WechatPrepayCommand(
                    "PAY1", "TR1", new BigDecimal("8.00"), "https://mall.example.com/notify", null));
    }

    @Test
    void prepayCommandRejectsBlankOrInvalidFields() {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new WechatPrepayCommand(
                    " ", "TR1", BigDecimal.ONE, "https://mall.example.com/notify", null));
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new WechatPrepayCommand(
                    "PAY1", null, BigDecimal.ONE, "https://mall.example.com/notify", null));
        assertThrows(
            IllegalArgumentException.class,
            () -> new WechatPrepayCommand("PAY1", "TR1", BigDecimal.ONE, " ", null));
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new WechatPrepayCommand(
                    "PAY1", "TR1", new BigDecimal("8.001"), "https://mall.example.com/notify", null));
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new WechatPrepayCommand(
                    "PAY1", "TR1", BigDecimal.ZERO, "https://mall.example.com/notify", null));
    }

    @Test
    void refundCommandRejectsAmountAboveOriginalPayment() {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new WechatRefundCommand(
                    "PR1", "PAY1", new BigDecimal("9.00"), new BigDecimal("8.00"), "售后"));
        assertDoesNotThrow(
            () ->
                new WechatRefundCommand(
                    "PR1", "PAY1", new BigDecimal("8.00"), new BigDecimal("8.00"), "售后"));
    }
}
