package com.lemnt.member.application.user.qry;

import java.io.Serializable;

/**
 * 查询会员资料分页查询。
 */
public record MemberPageQry(
        String mobile,
        int pageNum,
        int pageSize
) implements Serializable {}
