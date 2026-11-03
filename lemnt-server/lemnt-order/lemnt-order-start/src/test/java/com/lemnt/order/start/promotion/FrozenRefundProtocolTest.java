package com.lemnt.order.start.promotion;

import com.lemnt.member.client.MemberGrowthClient;
import com.lemnt.member.client.bo.MemberTradeAssetBo;
import com.lemnt.member.client.dto.MemberPointConfigDTO;
import com.lemnt.order.domain.member.model.MemberTradeChange;
import com.lemnt.order.infrastructure.member.gateway.MemberTradeGatewayImpl;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 防止旧会员服务丢弃新字段后以旧规则返回成功。 */
class FrozenRefundProtocolTest {
    @Test
    void oldConsumerMustRejectFrozenRefundInsteadOfCallingLegacyApply() {
        var oldCalled = new AtomicBoolean();
        var legacy =
                new MemberGrowthClient() {
                    public MemberPointConfigDTO getPointConfig() {
                        return null;
                    }

                    public void applyTrade(MemberTradeAssetBo value) {
                        oldCalled.set(true);
                    }
                };
        var gateway = new MemberTradeGatewayImpl();
        ReflectionTestUtils.setField(gateway, "client", legacy);
        assertThrows(
                UnsupportedOperationException.class,
                () ->
                        gateway.apply(
                                new MemberTradeChange(
                                        1L, "REFUND", "T1", "RF1", 5, 3, 1000, 333, false, 1, 1L,
                                        1)));
        assertFalse(oldCalled.get());
    }

    @Test
    void newConsumerReceivesExactFrozenPointsAndNoConfigRead() {
        var client = mock(MemberGrowthClient.class);
        var gateway = new MemberTradeGatewayImpl();
        ReflectionTestUtils.setField(gateway, "client", client);
        gateway.apply(
                new MemberTradeChange(1L, "REFUND", "T1", "RF1", 5, 3, 1000, 333, false, 1, 1L, 1));
        verify(client)
                .applyFrozenRefund(
                        argThat(
                                p ->
                                        p.refundPoint() == 1
                                                && p.deductGivePoint() == 1
                                                && p.refundRuleVersion() == 1));
        verify(client, never()).applyTrade(any());
        verify(client, never()).getPointConfig();
    }
}
