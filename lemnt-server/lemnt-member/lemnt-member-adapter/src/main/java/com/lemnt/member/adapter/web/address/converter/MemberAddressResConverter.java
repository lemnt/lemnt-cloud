package com.lemnt.member.adapter.web.address.converter;

import com.lemnt.member.adapter.web.address.res.AddressRes;
import com.lemnt.member.application.address.dto.AddressDTO;

/** 会员地址转换器。 */
public final class MemberAddressResConverter {

    private MemberAddressResConverter() {}

    /**
     * 地址。
     */
    public static AddressRes address(AddressDTO v) {
        return v == null
                ? null
                : new AddressRes(
                        v.id(),
                        v.memberId(),
                        v.name(),
                        v.mobile(),
                        v.areaId(),
                        v.detailAddress(),
                        v.defaultStatus());
    }
}
