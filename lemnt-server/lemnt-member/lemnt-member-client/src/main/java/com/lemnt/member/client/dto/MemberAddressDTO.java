package com.lemnt.member.client.dto;

import java.io.Serializable;

/** 仅本人可读的收货地址。 */
public record MemberAddressDTO(
        Long id,
        Long memberId,
        String name,
        String mobile,
        Long areaId,
        String detailAddress
) implements Serializable {
}
