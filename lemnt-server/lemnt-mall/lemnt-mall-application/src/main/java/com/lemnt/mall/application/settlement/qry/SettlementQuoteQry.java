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
        Long addressId,
        Integer deliveryType,
        Long pickUpStoreId
) implements Serializable {
    /** 普通快递报价。 */
    public SettlementQuoteQry(Long memberId, List<Long> productIds, Long couponId, Boolean pointStatus,
            Long addressId) {
        this(memberId, productIds, couponId, pointStatus, addressId, 1, null);
    }
    public SettlementQuoteQry(Long memberId, List<Long> productIds, Long couponId) {
        this(memberId, productIds, couponId, false, null, 1, null);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public SettlementQuoteQry(Long memberId, List<Long> productIds, Long couponId, Boolean pointStatus) {
        this(memberId, productIds, couponId, pointStatus, null, 1, null);
    }
}
