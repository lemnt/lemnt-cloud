package com.lemnt.member.infrastructure.address.converter;

import com.lemnt.member.domain.address.model.Address;
import com.lemnt.member.infrastructure.address.dataobject.AddressDO;

/**
 * 地址转换器。
 */
public final class AddressConverter {
    private AddressConverter() {}

    /**
     * 转换为领域对象。
     */
    public static Address toEntity(AddressDO row) {
        return row == null
                ? null
                : new Address(
                        row.getId(),
                        row.getUserId(),
                        row.getName(),
                        row.getMobile(),
                        row.getAreaId(),
                        row.getDetailAddress(),
                        row.getDefaultStatus());
    }

    /**
     * 转换为持久化数据对象。
     */
    public static AddressDO toDO(Address value) {
        var row = new AddressDO();
        row.setId(value.id());
        row.setUserId(value.memberId());
        row.setName(value.name());
        row.setMobile(value.mobile());
        row.setAreaId(value.areaId());
        row.setDetailAddress(value.detailAddress());
        row.setDefaultStatus(value.defaultStatus());
        return row;
    }
}
