package com.lemnt.member.application.address.executor;

import com.lemnt.member.application.address.service.MemberAddressQueryService;
import com.lemnt.member.application.address.dto.AddressDTO;
import com.lemnt.member.application.address.qry.AddressListQry;
import com.lemnt.member.domain.address.gateway.MemberAddressGateway;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 地址列表查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberAddressListQueryExe {

    private final MemberAddressGateway memberAddressGateway;
    private final MemberAddressQueryService memberAddressQueryService;

    /**
     * 执行地址列表查询。
     */
    public List<AddressDTO> execute(AddressListQry qry) {
        MemberRules.id(qry.memberId());
        return memberAddressQueryService.enrich(memberAddressGateway.addresses(qry.memberId()));
    }
}
