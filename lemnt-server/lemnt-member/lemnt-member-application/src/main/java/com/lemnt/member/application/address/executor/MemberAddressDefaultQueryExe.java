package com.lemnt.member.application.address.executor;

import com.lemnt.member.application.address.converter.MemberAddressDTOConverter;
import com.lemnt.member.application.address.dto.AddressDTO;
import com.lemnt.member.application.address.qry.AddressDefaultQry;
import com.lemnt.member.domain.address.gateway.MemberAddressGateway;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 默认地址查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberAddressDefaultQueryExe {

    private final MemberAddressGateway memberAddressGateway;

    /**
     * 执行默认地址查询。
     */
    public AddressDTO execute(AddressDefaultQry qry) {
        MemberRules.id(qry.memberId());
        return memberAddressGateway.addresses(qry.memberId()).stream()
                .filter(v -> Boolean.TRUE.equals(v.defaultStatus()))
                .findFirst()
                .map(MemberAddressDTOConverter::toAddressDTO)
                .orElse(null);
    }
}
