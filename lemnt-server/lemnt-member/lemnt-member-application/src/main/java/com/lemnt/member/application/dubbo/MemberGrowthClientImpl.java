package com.lemnt.member.application.dubbo;

import com.lemnt.member.client.MemberGrowthClient;
import com.lemnt.member.client.bo.MemberTradeAssetBo;
import com.lemnt.member.client.dto.MemberPointConfigDTO;
import com.lemnt.member.application.growth.cmd.MemberTradeAssetCmd;
import com.lemnt.member.application.growth.service.MemberGrowthService;
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
        return new MemberPointConfigDTO(config.pointTradeDeductEnable(), config.pointTradeDeductUnitPrice(),
                config.pointTradeDeductMaxPrice(), config.pointTradeGivePoint());
    }

    @Override
    public void applyTrade(MemberTradeAssetBo v) {
        growth.tradeAsset(new MemberTradeAssetCmd(v.memberId(), v.operation(), v.orderNo(), v.refundNo(),
                v.usePoint(), v.givePoint(), v.paidFen(), v.refundFen(), v.fullRefund()));
    }
}
