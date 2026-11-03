package com.lemnt.member.application.dubbo;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.application.growth.cmd.MemberTradeAssetCmd;
import com.lemnt.member.application.growth.service.MemberGrowthService;
import com.lemnt.member.client.MemberGrowthClient;
import com.lemnt.member.client.bo.MemberTradeAssetBo;
import com.lemnt.member.client.dto.MemberPointConfigDTO;
import com.lemnt.member.domain.growth.gateway.MemberConfigGateway;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

/** 远程契约转换为成长应用命令，不跨服务共享会员事务。 */
@DubboService
@RequiredArgsConstructor
public class MemberGrowthClientImpl implements MemberGrowthClient {
    private final MemberGrowthService growth;
    private final MemberConfigGateway configGateway;

    @Override
    public MemberPointConfigDTO getPointConfig() {
        var config = configGateway.config();
        if (config == null) {
            throw new ServiceException("会员积分配置未设置", 409);
        }
        return new MemberPointConfigDTO(
                config.pointTradeDeductEnable(),
                config.pointTradeDeductUnitPrice(),
                config.pointTradeDeductMaxPrice(),
                config.pointTradeGivePoint());
    }

    @Override
    public void applyTrade(MemberTradeAssetBo value) {
        if (value == null || value.refundRuleVersion() != null) {
            throw new IllegalArgumentException("冻结退款必须使用专用协议入口");
        }
        apply(value);
    }

    @Override
    public void applyFrozenRefund(MemberTradeAssetBo value) {
        if (value == null
                || !"REFUND".equals(value.operation())
                || !Integer.valueOf(1).equals(value.refundRuleVersion())
                || value.deductGivePoint() == null) {
            throw new IllegalArgumentException("冻结退款协议不合法");
        }
        apply(value);
    }

    /** 两种协议统一映射完整字段，业务规则由成长领域执行。 */
    private void apply(MemberTradeAssetBo v) {
        growth.tradeAsset(
                new MemberTradeAssetCmd(
                        v.memberId(),
                        v.operation(),
                        v.orderNo(),
                        v.refundNo(),
                        v.usePoint(),
                        v.givePoint(),
                        v.paidFen(),
                        v.refundFen(),
                        v.fullRefund(),
                        v.refundPoint(),
                        v.deductGivePoint(),
                        v.refundRuleVersion()));
    }
}
