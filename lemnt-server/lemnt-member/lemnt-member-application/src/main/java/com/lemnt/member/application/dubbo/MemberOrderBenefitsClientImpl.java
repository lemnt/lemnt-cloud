package com.lemnt.member.application.dubbo;

import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.member.client.MemberOrderBenefitsClient;
import com.lemnt.member.domain.event.gateway.MessageInboxGateway;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 只有会员本地业务成功提交后，回执才对外可见。 */
@DubboService
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class MemberOrderBenefitsClientImpl implements MemberOrderBenefitsClient {
    private final MessageInboxGateway inbox;

    public boolean paidApplied(String no) {
        return inbox.has("member.order_paid.v1", CommerceEventTopics.ORDER_PAID, no);
    }

    public boolean refundApplied(String no) {
        return inbox.has(
                "member.after_sale_completed.v1", CommerceEventTopics.AFTER_SALE_COMPLETED, no);
    }
}
