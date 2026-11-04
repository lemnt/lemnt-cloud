package com.lemnt.order.infrastructure.brokerage.gateway;

import com.lemnt.member.client.MemberUserClient;
import com.lemnt.order.domain.brokerage.gateway.BrokerageMemberGateway;
import com.lemnt.order.domain.brokerage.model.BrokerageMemberInfo;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 通过会员服务读取昵称头像，失败不影响佣金数据。
 */
@Component
public class BrokerageMemberGatewayImpl implements BrokerageMemberGateway {
    @DubboReference(check = false)
    private MemberUserClient memberUserClient;

    @Override
    public BrokerageMemberInfo find(Long memberId) {
        var member = memberUserClient.getByMemberId(memberId);
        return member == null
            ? null
            : new BrokerageMemberInfo(member.id(), member.nickname(), member.avatar());
    }
}
