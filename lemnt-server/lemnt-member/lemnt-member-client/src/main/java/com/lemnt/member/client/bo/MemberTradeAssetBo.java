package com.lemnt.member.client.bo;

/** 交易服务确认的订单积分及经验变更，金额均为整数分。 */
public record MemberTradeAssetBo(Long memberId, String operation, String orderNo, String refundNo,
        long usePoint, long givePoint, long paidFen, long refundFen, boolean fullRefund,
        long refundPoint) implements java.io.Serializable {
    /** 兼容现金退款调用，零元订单使用 refundPoint。 */
    public MemberTradeAssetBo(Long memberId, String operation, String orderNo, String refundNo,
            long usePoint, long givePoint, long paidFen, long refundFen, boolean fullRefund) {
        this(memberId, operation, orderNo, refundNo, usePoint, givePoint, paidFen, refundFen,
                fullRefund, 0L);
    }
}
