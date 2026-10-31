package com.lemnt.member.application.address.qry;

import java.io.Serializable;

/**
 * 默认地址查询。
 */
public record AddressDefaultQry(
        Long memberId
) implements Serializable {}
