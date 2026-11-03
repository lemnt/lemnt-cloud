package com.lemnt.member.client;

import com.lemnt.member.client.bo.MemberTradeAssetBo;
import com.lemnt.member.client.dto.MemberPointConfigDTO;

/** 会员成长领域供交易使用的配置与资产变更契约。 */
public interface MemberGrowthClient {
    MemberPointConfigDTO getPointConfig();

    void applyTrade(MemberTradeAssetBo value);

    /** 冻结退款专用入口，旧消费者必须拒绝，不能忽略新字段后按旧规则退积分。 */
    default void applyFrozenRefund(MemberTradeAssetBo value) {
        throw new UnsupportedOperationException("会员服务未升级冻结退款协议");
    }
}
