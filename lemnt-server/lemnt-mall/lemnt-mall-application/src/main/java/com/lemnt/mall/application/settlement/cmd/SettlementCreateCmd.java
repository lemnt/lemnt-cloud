package com.lemnt.mall.application.settlement.cmd;

import java.io.Serializable;
import java.util.List;

/** SettlementCreateCmd 输入契约。 */
public record SettlementCreateCmd(Long memberId,
        List<Long> productIds,
        Long couponId,
        Boolean pointStatus,
        Long addressId
) implements Serializable {
    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public SettlementCreateCmd(Long memberId, List<Long> productIds, Long couponId) {
        this(memberId, productIds, couponId, false, null);
    }
}
