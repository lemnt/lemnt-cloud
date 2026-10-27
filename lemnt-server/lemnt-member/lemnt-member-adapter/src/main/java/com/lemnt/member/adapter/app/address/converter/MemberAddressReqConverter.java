package com.lemnt.member.adapter.app.address.converter;

import com.lemnt.member.adapter.app.address.req.AppMemberAddressCreateReq;
import com.lemnt.member.adapter.app.address.req.AppMemberAddressUpdateReq;
import com.lemnt.member.application.address.cmd.AddressSaveCmd;

/** 会员地址转换器。 */
public final class MemberAddressReqConverter {

    private MemberAddressReqConverter() {}

    /**
     * 地址。
     */
    public static AddressSaveCmd address(Long memberId, Long id, AppMemberAddressCreateReq r) {
        return new AddressSaveCmd(
                id,
                memberId,
                r.name(),
                r.mobile(),
                r.areaId(),
                r.detailAddress(),
                r.defaultStatus());
    }

    /**
     * 地址。
     */
    public static AddressSaveCmd address(Long memberId, Long id, AppMemberAddressUpdateReq r) {
        return new AddressSaveCmd(
                id,
                memberId,
                r.name(),
                r.mobile(),
                r.areaId(),
                r.detailAddress(),
                r.defaultStatus());
    }
}
