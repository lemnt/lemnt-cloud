package com.lemnt.member.client.bo;

/** 交易服务确认的订单积分及经验变更，金额均为整数分。 */
public record MemberTradeAssetBo(Long memberId, String operation, String orderNo, String refundNo,
        long usePoint, long givePoint, long paidFen, long refundFen, boolean fullRefund) implements java.io.Serializable {}
