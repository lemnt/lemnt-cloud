package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.Address;
import com.lemnt.member.infrastructure.account.dataobject.AddressDO;

public final class AddressConverter {
    private AddressConverter() {}

    public static Address domain(AddressDO row) {
        return row == null
                ? null
                : new Address(
                        row.getId(),
                        row.getMemberId(),
                        row.getName(),
                        row.getMobile(),
                        row.getAreaId(),
                        row.getDetailAddress(),
                        row.getDefaultStatus());
    }

    public static AddressDO row(Address value) {
        var row = new AddressDO();
        row.setId(value.id());
        row.setMemberId(value.memberId());
        row.setName(value.name());
        row.setMobile(value.mobile());
        row.setAreaId(value.areaId());
        row.setDetailAddress(value.detailAddress());
        row.setDefaultStatus(value.defaultStatus());
        return row;
    }
}
