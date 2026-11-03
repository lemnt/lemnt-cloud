package com.lemnt.product.infrastructure.member.gateway;

import com.lemnt.member.client.MemberUserClient;
import com.lemnt.member.client.dto.MemberUserDTO;
import com.lemnt.product.domain.member.gateway.MemberLookupGateway;
import com.lemnt.product.domain.member.model.MemberSnapshot;

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
        return member == null
                ? null
                : new MemberSnapshot(
                        member.id(),
                        member.status(),
                        member.discountPercent(),
                        member.points(),
                        member.nickname(),
                        member.avatar());
    }
}
