package com.lemnt.auth.infrastructure.member.gateway;

import com.lemnt.auth.domain.member.gateway.MemberAccountGateway;
import com.lemnt.auth.domain.member.model.MemberLoginInfo;
import com.lemnt.member.client.MemberUserClient;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 会员登录资料 RPC 适配器，不持有数据源或数据库事务。 */
@Component
public class MemberAccountGatewayImpl implements MemberAccountGateway {
    @DubboReference
    private MemberUserClient memberUserClient;

    @Override
    public MemberLoginInfo findByMobile(String mobile) {
        var value = memberUserClient.getByMobile(mobile);
        return value == null ? null : new MemberLoginInfo(value.memberId(), value.mobile(),
                value.status(), value.passwordHash());
    }

    @Override
    public Integer findStatusByMemberId(Long memberId) {
        var member = memberUserClient.getByMemberId(memberId);
        return member == null ? null : member.status();
    }
}
