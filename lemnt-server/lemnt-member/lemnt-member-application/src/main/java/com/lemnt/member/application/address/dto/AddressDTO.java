package com.lemnt.member.application.address.dto;

import java.io.Serializable;

/**
 * 地址数据传输对象。
 */
public record AddressDTO(
        Long id,
        Long memberId,
        String name,
        String mobile,
        Long areaId,
        String detailAddress,
        Boolean defaultStatus
) implements Serializable {}
