package com.lemnt.mall.application.settlement.cmd;

import java.io.Serializable;
import java.util.List;

/** SettlementCreateCmd 输入契约。 */
public record SettlementCreateCmd(Long memberId,
        List<Long> productIds,
        Long couponId,
        Boolean pointStatus,
        Long addressId,
        Integer deliveryType,
        Long pickUpStoreId
) implements Serializable {
    /** 兼容普通快递报价。 */
    public SettlementCreateCmd(Long memberId, List<Long> productIds, Long couponId, Boolean pointStatus,
            Long addressId) {
        this(memberId, productIds, couponId, pointStatus, addressId, 1, null);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public SettlementCreateCmd(Long memberId, List<Long> productIds, Long couponId) {
        this(memberId, productIds, couponId, false, null, 1, null);
    }
}
