package com.lemnt.member.application.address.converter;

import com.lemnt.member.application.address.dto.AddressDTO;
import com.lemnt.member.domain.address.model.Address;

/** 会员地址转换器。 */
public final class MemberAddressDTOConverter {

    private MemberAddressDTOConverter() {}

    /**
     * 地址。
     */
    public static AddressDTO toAddressDTO(Address v) {
        return v == null
                ? null
                : new AddressDTO(
                        v.id(),
                        v.memberId(),
                        v.name(),
                        v.mobile(),
                        v.areaId(),
                        v.detailAddress(),
                        v.defaultStatus());
    }
}
