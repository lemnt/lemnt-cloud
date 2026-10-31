package com.lemnt.mall.infrastructure.member.gateway;

import com.lemnt.mall.domain.member.gateway.MemberTradeGateway;
import com.lemnt.mall.domain.member.model.MemberPointConfig;
import com.lemnt.mall.domain.member.model.MemberTradeChange;
import com.lemnt.member.client.MemberGrowthClient;
import com.lemnt.member.client.bo.MemberTradeAssetBo;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 通过成长领域客户端处理会员事务，商城不读写会员表。 */
@Component
public class MemberTradeGatewayImpl implements MemberTradeGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private MemberGrowthClient client;

    @Override
    public MemberPointConfig config() {
        var config = client.getPointConfig();
        if (config == null) {
            throw new IllegalStateException("会员积分配置未返回");
        }
        return new MemberPointConfig(config.enabled(), config.unitPrice(), config.maxPoints(), config.givePerYuan());
    }

    @Override
    public void apply(MemberTradeChange c) {
        client.applyTrade(new MemberTradeAssetBo(c.memberId(), c.operation(), c.orderNo(), c.refundNo(),
                c.usePoint(), c.givePoint(), c.paidFen(), c.refundFen(), c.fullRefund()));
    }
}
