package com.lemnt.member.application.address.qry;

import java.io.Serializable;

/**
 * 地址详情查询。
 */
public record AddressDetailQry(
        Long memberId,
        Long id
) implements Serializable {}
