package com.lemnt.member.application.address.cmd;

import java.io.Serializable;

/**
 * 地址删除命令。
 */
public record AddressDeleteCmd(
        Long memberId,
        Long id
) implements Serializable {}
