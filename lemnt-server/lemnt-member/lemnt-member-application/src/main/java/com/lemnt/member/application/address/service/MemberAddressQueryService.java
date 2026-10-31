package com.lemnt.member.application.address.service;

import com.lemnt.member.application.address.converter.MemberAddressDTOConverter;
import com.lemnt.member.application.address.dto.AddressDTO;
import com.lemnt.member.domain.address.gateway.MemberAddressAreaGateway;
import com.lemnt.member.domain.address.model.Address;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.List;

/** 地址查询统一批量返显地区名称，避免逐行远程查询。 */
@Component
@RequiredArgsConstructor
public class MemberAddressQueryService {

    private final MemberAddressAreaGateway memberAddressAreaGateway;

    /** 整个地址列表只按去重地区编号查询一次，空列表不查询。 */
    public List<AddressDTO> enrich(List<Address> rows) {
        if (rows.isEmpty()) return List.of();
        var ids = rows.stream().map(Address::areaId).distinct().toList();
        var names = memberAddressAreaGateway.names(ids);
        return rows.stream().map(row -> MemberAddressDTOConverter.toAddressDTO(row, names.get(row.areaId()))).toList();
    }

    /** 缺失地址直接返回null，不触发地区读取。 */
    public AddressDTO enrich(Address row) {
        return row == null ? null : enrich(List.of(row)).get(0);
    }
}
