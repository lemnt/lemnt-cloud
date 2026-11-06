package com.lemnt.order.start.support;

import com.lemnt.member.client.MemberAddressClient;
import com.lemnt.member.client.MemberGrowthClient;
import com.lemnt.member.client.MemberOrderBenefitsClient;
import com.lemnt.member.client.MemberUserClient;
import com.lemnt.member.client.dto.MemberAddressDTO;
import com.lemnt.member.client.dto.MemberPointConfigDTO;
import com.lemnt.member.client.dto.MemberUserDTO;
import com.lemnt.order.domain.config.gateway.TradeConfigGateway;
import com.lemnt.order.start.BusinessConfigTestSupport;
import com.lemnt.pay.application.payment.executor.PaymentPrepayCmdExe;
import com.lemnt.pay.domain.channel.gateway.PaymentChannelCloseGateway;

import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 会员和外部支付渠道为传输替身；四个被拆分上下文使用生产实现和独立数据库。
 */
public final class CommerceTestDefaults {
    private CommerceTestDefaults() {
    }

    public static void install(
        CommerceTestFixture fixture,
        CommerceEventPump events,
        LongSupplier points,
        Supplier<MemberPointConfigDTO> config,
        Supplier<MemberAddressDTO> address) {
        var users = mock(MemberUserClient.class);
        when(users.getByMemberId(anyLong()))
            .thenAnswer(
                v ->
                    new MemberUserDTO(
                        v.getArgument(0),
                        "测试会员",
                        1,
                        100,
                        points.getAsLong(),
                        null));
        fixture.remote(MemberUserClient.class, users);
        var growth = mock(MemberGrowthClient.class);
        when(growth.getPointConfig()).thenAnswer(v -> config.get());
        fixture.remote(MemberGrowthClient.class, growth);
        fixture.remote(
            MemberAddressClient.class,
            (owner, id) -> {
                var v = address.get();
                return v != null && owner.equals(v.memberId()) && id.equals(v.id()) ? v : null;
            });
        var benefits = mock(MemberOrderBenefitsClient.class);
        when(benefits.paidApplied(anyString()))
            .thenAnswer(v -> events.memberPaid.contains(v.<String>getArgument(0)));
        when(benefits.refundApplied(anyString()))
            .thenAnswer(v -> events.memberRefunded.contains(v.<String>getArgument(0)));
        fixture.remote(MemberOrderBenefitsClient.class, benefits);
        fixture.register(PaymentPrepayCmdExe.class, mock(PaymentPrepayCmdExe.class));
        fixture.register(PaymentChannelCloseGateway.class, payment -> true);
        fixture.register(TradeConfigGateway.class, BusinessConfigTestSupport.gateway());
    }
}
