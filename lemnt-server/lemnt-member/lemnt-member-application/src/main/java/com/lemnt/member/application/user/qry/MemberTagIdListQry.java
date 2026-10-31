package com.lemnt.member.application.user.qry;

import java.io.Serializable;

/**
 * 会员查询标签查询。
 */
public record MemberTagIdListQry(
        Long memberId
) implements Serializable {}
