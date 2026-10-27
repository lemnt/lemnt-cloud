package com.lemnt.member.adapter.app.address.converter;

import com.lemnt.member.adapter.app.address.res.AppMemberAddressCreateRes;
import com.lemnt.member.adapter.app.address.res.AppMemberAddressDefaultAddressRes;
import com.lemnt.member.adapter.app.address.res.AppMemberAddressDetailRes;
import com.lemnt.member.adapter.app.address.res.AppMemberAddressListRes;
import com.lemnt.member.adapter.app.address.res.AppMemberAddressUpdateRes;
import com.lemnt.member.application.address.dto.AddressDTO;

/** 会员地址转换器。 */
public final class MemberAddressResConverter {

    private MemberAddressResConverter() {}

    /**
     * 地址。
     */
    public static AppMemberAddressListRes toAppMemberAddressListRes(AddressDTO v) {
        return v == null
                ? null
                : new AppMemberAddressListRes(
                        v.id(),
                        v.memberId(),
                        v.name(),
                        v.mobile(),
                        v.areaId(),
                        v.detailAddress(),
                        v.defaultStatus());
    }

    /**
     * 地址。
     */
    public static AppMemberAddressDefaultAddressRes toAppMemberAddressDefaultAddressRes(AddressDTO v) {
        return v == null
                ? null
                : new AppMemberAddressDefaultAddressRes(
                        v.id(),
                        v.memberId(),
                        v.name(),
                        v.mobile(),
                        v.areaId(),
                        v.detailAddress(),
                        v.defaultStatus());
    }

    /**
     * 地址。
     */
    public static AppMemberAddressDetailRes toAppMemberAddressDetailRes(AddressDTO v) {
        return v == null
                ? null
                : new AppMemberAddressDetailRes(
                        v.id(),
                        v.memberId(),
                        v.name(),
                        v.mobile(),
                        v.areaId(),
                        v.detailAddress(),
                        v.defaultStatus());
    }

    /**
     * 地址。
     */
    public static AppMemberAddressCreateRes toAppMemberAddressCreateRes(AddressDTO v) {
        return v == null
                ? null
                : new AppMemberAddressCreateRes(
                        v.id(),
                        v.memberId(),
                        v.name(),
                        v.mobile(),
                        v.areaId(),
                        v.detailAddress(),
                        v.defaultStatus());
    }

    /**
     * 地址。
     */
    public static AppMemberAddressUpdateRes toAppMemberAddressUpdateRes(AddressDTO v) {
        return v == null
                ? null
                : new AppMemberAddressUpdateRes(
                        v.id(),
                        v.memberId(),
                        v.name(),
                        v.mobile(),
                        v.areaId(),
                        v.detailAddress(),
                        v.defaultStatus());
    }
}
