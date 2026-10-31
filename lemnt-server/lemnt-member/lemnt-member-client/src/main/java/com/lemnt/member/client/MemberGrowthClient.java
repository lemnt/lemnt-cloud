package com.lemnt.member.client;

import com.lemnt.member.client.bo.MemberTradeAssetBo;
import com.lemnt.member.client.dto.MemberPointConfigDTO;

/** 会员成长领域供交易使用的配置与资产变更契约。 */
public interface MemberGrowthClient {
    MemberPointConfigDTO getPointConfig();
    void applyTrade(MemberTradeAssetBo value);
}
