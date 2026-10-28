package com.lemnt.member.application.growth.cmd;

/** 订单生命周期的会员权益命令。 */
public record MemberTradeAssetCmd(Long memberId, String operation, String orderNo, String refundNo,
        long usePoint, long givePoint, long paidFen, long refundFen, boolean fullRefund) {}
