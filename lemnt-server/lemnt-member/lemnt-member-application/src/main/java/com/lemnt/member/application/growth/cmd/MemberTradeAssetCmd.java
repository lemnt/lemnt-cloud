package com.lemnt.member.application.growth.cmd;

/** 订单生命周期的会员权益命令。 */
public record MemberTradeAssetCmd(Long memberId, String operation, String orderNo, String refundNo,
        long usePoint, long givePoint, long paidFen, long refundFen, boolean fullRefund,
        long refundPoint) {
    /** 兼容现金退款调用，零元订单使用 refundPoint。 */
    public MemberTradeAssetCmd(Long memberId, String operation, String orderNo, String refundNo,
            long usePoint, long givePoint, long paidFen, long refundFen, boolean fullRefund) {
        this(memberId, operation, orderNo, refundNo, usePoint, givePoint, paidFen, refundFen,
                fullRefund, 0L);
    }
}
