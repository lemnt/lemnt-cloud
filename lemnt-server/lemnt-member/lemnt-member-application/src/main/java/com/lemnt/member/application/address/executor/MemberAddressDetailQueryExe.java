package com.lemnt.member.application.address.executor;

import com.lemnt.member.application.address.converter.MemberAddressDTOConverter;
import com.lemnt.member.application.address.dto.AddressDTO;
import com.lemnt.member.application.address.qry.AddressDetailQry;
import com.lemnt.member.domain.address.gateway.MemberAddressGateway;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 地址详情查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberAddressDetailQueryExe {

    private final MemberAddressGateway memberAddressGateway;

    /**
     * 执行地址详情查询。
     */
    public AddressDTO execute(AddressDetailQry qry) {
        MemberRules.id(qry.memberId());
        MemberRules.id(qry.id());
        return MemberAddressDTOConverter.toAddressDTO(memberAddressGateway.address(qry.memberId(), qry.id()));
    }
}
