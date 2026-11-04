package com.lemnt.member.client;

/**
 * 真实消费回执，消息发送成功不能代替会员权益到账。
 */
public interface MemberOrderBenefitsClient {
    boolean paidApplied(String orderNo);

    boolean refundApplied(String afterSaleNo);
}
