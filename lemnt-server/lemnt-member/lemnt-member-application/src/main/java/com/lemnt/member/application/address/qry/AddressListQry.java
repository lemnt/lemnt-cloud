package com.lemnt.member.application.address.qry;

import java.io.Serializable;

/**
 * 地址列表查询。
 */
public record AddressListQry(
        Long memberId
) implements Serializable {}
