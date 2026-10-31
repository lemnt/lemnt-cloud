package com.lemnt.mall.domain.member.model;

/** 订单生命周期已确认的会员权益事实。 */
public record MemberTradeChange(Long memberId, String operation, String orderNo, String refundNo,
        long usePoint, long givePoint, long paidFen, long refundFen, boolean fullRefund,
        long refundPoint) {
    /** 兼容现金退款，零元订单由 refundPoint 指定本次退回积分。 */
    public MemberTradeChange(Long memberId, String operation, String orderNo, String refundNo,
            long usePoint, long givePoint, long paidFen, long refundFen, boolean fullRefund) {
        this(memberId, operation, orderNo, refundNo, usePoint, givePoint, paidFen, refundFen,
                fullRefund, 0L);
    }
}
