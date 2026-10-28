package com.lemnt.mall.application.settlement.qry;

import java.io.Serializable;
import java.util.List;

/**
 * 结算报价查询。
 */
public record SettlementQuoteQry(
        Long memberId,
        List<Long> productIds,
        Long couponId,
        Boolean pointStatus,
        Long addressId
) implements Serializable {
    public SettlementQuoteQry(Long memberId, List<Long> productIds, Long couponId) {
        this(memberId, productIds, couponId, false);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public SettlementQuoteQry(Long memberId, List<Long> productIds, Long couponId, Boolean pointStatus) {
        this(memberId, productIds, couponId, pointStatus, null);
    }
}
