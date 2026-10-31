package com.lemnt.trade.application.payment;

import com.lemnt.trade.application.payment.cmd.PayAppCreateCmd;
import com.lemnt.trade.application.payment.cmd.PayChannelCreateCmd;
import com.lemnt.trade.application.payment.cmd.PayChannelUpdateCmd;
import com.lemnt.trade.application.payment.executor.PayAppCreateCmdExe;
import com.lemnt.trade.application.payment.executor.PayAppDeleteCmdExe;
import com.lemnt.trade.application.payment.executor.PayChannelCreateCmdExe;
import com.lemnt.trade.application.payment.executor.PayChannelUpdateCmdExe;
import com.lemnt.trade.domain.payment.gateway.PayAppGateway;
import com.lemnt.trade.domain.payment.gateway.PayChannelConfigGateway;
import com.lemnt.trade.domain.payment.model.PayApp;
import com.lemnt.trade.domain.payment.model.PayChannelConfig;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 支付应用与渠道配置的校验、唯一性与密钥保留规则。 */
class PayConfigExeTest {
    final PayAppGateway apps = mock(PayAppGateway.class);
    final PayChannelConfigGateway channels = mock(PayChannelConfigGateway.class);
    final PayAppCreateCmdExe appCreate = new PayAppCreateCmdExe(apps);
    final PayAppDeleteCmdExe appDelete = new PayAppDeleteCmdExe(apps, channels);
    final PayChannelCreateCmdExe channelCreate = new PayChannelCreateCmdExe(apps, channels);
    final PayChannelUpdateCmdExe channelUpdate = new PayChannelUpdateCmdExe(channels);

    private PayApp app(Long id, String appKey) {
        return new PayApp(id, appKey, "应用", 0, null, null, null, null);
    }

    private PayChannelConfig channel(Long id, String config) {
        return new PayChannelConfig(id, 1L, "ALIPAY", 0, BigDecimal.ONE, null, config, null);
    }

    @Test
    void appCreateRejectsInvalidKeyDuplicateAndBadUrl() {
        assertThrows(IllegalArgumentException.class, () -> appCreate.execute(
                new PayAppCreateCmd("ab", "应用", 0, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> appCreate.execute(
                new PayAppCreateCmd("app_key", "应用", 2, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> appCreate.execute(
                new PayAppCreateCmd("app_key", "应用", 0, null, "ftp://x", null)));
        when(apps.existsAppKey("app_key", null)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> appCreate.execute(
                new PayAppCreateCmd("app_key", "应用", 0, null, null, null)));
    }

    @Test
    void appCreatePersistsValidApp() {
        when(apps.existsAppKey("app_key", null)).thenReturn(false);
        when(apps.insert(any())).thenReturn(app(9L, "app_key"));
        var value = appCreate.execute(new PayAppCreateCmd("app_key", "应用", 0, "备注",
                "https://example.test/pay", "https://example.test/refund"));
        assertEquals(9L, value.id());
        ArgumentCaptor<PayApp> captor = ArgumentCaptor.forClass(PayApp.class);
        verify(apps).insert(captor.capture());
        assertEquals("https://example.test/pay", captor.getValue().orderNotifyUrl());
    }

    @Test
    void appDeleteRejectedWhenChannelsReferenced() {
        when(apps.find(1L)).thenReturn(app(1L, "app_key"));
        when(channels.channelReferenced(1L)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> appDelete.execute(1L));
        verify(apps, never()).delete(any());
    }

    @Test
    void channelCreateRejectsMissingAppUnknownCodeAndDuplicate() {
        when(apps.find(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> channelCreate.execute(
                new PayChannelCreateCmd(1L, "ALIPAY", 0, null, null, null)));
        when(apps.find(1L)).thenReturn(app(1L, "app_key"));
        assertThrows(IllegalArgumentException.class, () -> channelCreate.execute(
                new PayChannelCreateCmd(1L, "UNION", 0, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> channelCreate.execute(
                new PayChannelCreateCmd(1L, "ALIPAY", 0, null, null, "{not-json}")));
        when(channels.exists(1L, "ALIPAY", null)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> channelCreate.execute(
                new PayChannelCreateCmd(1L, "ALIPAY", 0, null, null, "{\"merchant\":\"m\"}")));
    }

    @Test
    void channelCreateAcceptsJsonAndFeeWithinRange() {
        when(apps.find(1L)).thenReturn(app(1L, "app_key"));
        when(channels.insert(any())).thenReturn(channel(5L, "{\"merchant\":\"m\"}"));
        var value = channelCreate.execute(new PayChannelCreateCmd(1L, "WECHAT", 0, new BigDecimal("1.5"),
                "备注", "{\"merchant\":\"m\"}"));
        assertEquals(5L, value.id());
        ArgumentCaptor<PayChannelConfig> captor = ArgumentCaptor.forClass(PayChannelConfig.class);
        verify(channels).insert(captor.capture());
        assertEquals(new BigDecimal("1.50"), captor.getValue().feeRate());
        assertEquals("{\"merchant\":\"m\"}", captor.getValue().config());
    }

    @Test
    void channelUpdateKeepsSecretWhenConfigBlank() {
        when(channels.find(5L)).thenReturn(channel(5L, "{\"secret\":\"keep\"}"));
        when(channels.update(any())).thenReturn(true);
        channelUpdate.execute(new PayChannelUpdateCmd(5L, 0, BigDecimal.ONE, null, "  "));
        ArgumentCaptor<PayChannelConfig> captor = ArgumentCaptor.forClass(PayChannelConfig.class);
        verify(channels).update(captor.capture());
        assertEquals("{\"secret\":\"keep\"}", captor.getValue().config());
        assertEquals("ALIPAY", captor.getValue().code());
    }

    @Test
    void channelUpdateReplacesSecretWhenProvided() {
        when(channels.find(5L)).thenReturn(channel(5L, "{\"secret\":\"old\"}"));
        when(channels.update(any())).thenReturn(true);
        channelUpdate.execute(new PayChannelUpdateCmd(5L, 1, new BigDecimal("2.25"), "r", "{\"secret\":\"new\"}"));
        ArgumentCaptor<PayChannelConfig> captor = ArgumentCaptor.forClass(PayChannelConfig.class);
        verify(channels).update(captor.capture());
        assertEquals("{\"secret\":\"new\"}", captor.getValue().config());
        assertEquals(Integer.valueOf(1), captor.getValue().status());
        assertEquals(new BigDecimal("2.25"), captor.getValue().feeRate());
    }
}
