package com.lemnt.member.application.address.cmd;

import java.io.Serializable;

/**
 * 地址保存命令。
 */
public record AddressSaveCmd(
        Long id,
        Long memberId,
        String name,
        String mobile,
        Long areaId,
        String detailAddress,
        Boolean defaultStatus
) implements Serializable {}
