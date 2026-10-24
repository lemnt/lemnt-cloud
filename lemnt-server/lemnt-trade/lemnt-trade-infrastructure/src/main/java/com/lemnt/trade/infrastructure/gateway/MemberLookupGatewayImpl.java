package com.lemnt.trade.infrastructure.gateway;

import com.lemnt.member.client.MemberClient;
import com.lemnt.member.client.dto.MemberProfileDTO;
import com.lemnt.trade.domain.gateway.MemberLookupGateway;
import com.lemnt.trade.domain.model.MemberSnapshot;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Component
public class MemberLookupGatewayImpl implements MemberLookupGateway {
    @DubboReference(check = false)
    private MemberClient client;

    @Override
    public MemberSnapshot findById(Long memberId) {
        MemberProfileDTO member = client.findById(memberId);
        return member == null ? null : new MemberSnapshot(member.id(), member.status());
    }
}