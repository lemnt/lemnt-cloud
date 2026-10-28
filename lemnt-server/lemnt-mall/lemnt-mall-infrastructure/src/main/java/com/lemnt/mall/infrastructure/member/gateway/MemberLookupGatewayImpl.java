package com.lemnt.mall.infrastructure.member.gateway;

import com.lemnt.mall.domain.member.gateway.MemberLookupGateway;
import com.lemnt.mall.domain.member.model.MemberSnapshot;
import com.lemnt.member.client.MemberUserClient;
import com.lemnt.member.client.dto.MemberUserDTO;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 会员查询领域网关实现。
 */
@Component
public class MemberLookupGatewayImpl implements MemberLookupGateway {
    @DubboReference(check = false)
    private MemberUserClient memberUserClient;

    @Override
    public MemberSnapshot findById(Long memberId) {
        MemberUserDTO member = memberUserClient.getByMemberId(memberId);
        return member == null ? null : new MemberSnapshot(member.id(), member.status(), member.discountPercent(), member.points());
    }
}
