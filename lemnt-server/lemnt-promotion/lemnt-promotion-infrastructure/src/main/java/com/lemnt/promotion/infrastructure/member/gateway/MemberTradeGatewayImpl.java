package com.lemnt.promotion.infrastructure.member.gateway;

import com.lemnt.member.client.MemberGrowthClient;
import com.lemnt.member.client.bo.MemberTradeAssetBo;
import com.lemnt.promotion.domain.member.gateway.MemberTradeGateway;
import com.lemnt.promotion.domain.member.model.MemberPointConfig;
import com.lemnt.promotion.domain.member.model.MemberTradeChange;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 通过成长领域客户端处理会员事务，商城不读写会员表。
 */
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
        return new MemberPointConfig(
            config.enabled(), config.unitPrice(), config.maxPoints(), config.givePerYuan());
    }

    @Override
    public void apply(MemberTradeChange c) {
        var payload =
            new MemberTradeAssetBo(
                c.memberId(),
                c.operation(),
                c.orderNo(),
                c.refundNo(),
                c.usePoint(),
                c.givePoint(),
                c.paidFen(),
                c.refundFen(),
                c.fullRefund(),
                c.refundPoint(),
                c.deductGivePoint(),
                c.refundRuleVersion());
        if (c.refundRuleVersion() != null) {
            if (!Integer.valueOf(1).equals(c.refundRuleVersion())
                || !"REFUND".equals(c.operation())) {
                throw new IllegalArgumentException("冻结退款协议版本不合法");
            }
            client.applyFrozenRefund(payload);
        } else {
            client.applyTrade(payload);
        }
    }
}
