package com.lemnt.mall.domain.member.model;

/** 订单生命周期已确认的会员权益事实。 */
public record MemberTradeChange(Long memberId, String operation, String orderNo, String refundNo,
        long usePoint, long givePoint, long paidFen, long refundFen, boolean fullRefund) {}
