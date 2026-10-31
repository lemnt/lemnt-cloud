package com.lemnt.member.application.address.executor;

import com.lemnt.member.application.address.service.MemberAddressQueryService;
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
    private final MemberAddressQueryService memberAddressQueryService;

    /**
     * 执行地址详情查询。
     */
    public AddressDTO execute(AddressDetailQry qry) {
        MemberRules.id(qry.memberId());
        MemberRules.id(qry.id());
        return memberAddressQueryService.enrich(memberAddressGateway.address(qry.memberId(), qry.id()));
    }
}
