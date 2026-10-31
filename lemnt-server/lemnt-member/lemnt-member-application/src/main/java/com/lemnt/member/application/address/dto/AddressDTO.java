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
        Boolean defaultStatus,
        String areaName
) implements Serializable {
    /** 写结果及旧调用没有名称时保留未知，不推断地区。 */
    public AddressDTO(Long id, Long memberId, String name, String mobile, Long areaId,
            String detailAddress, Boolean defaultStatus) {
        this(id, memberId, name, mobile, areaId, detailAddress, defaultStatus, null);
    }
}
