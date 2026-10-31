package com.lemnt.member.application.user.qry;

import java.io.Serializable;

/**
 * 资料查询。
 */
public record MemberDetailQry(
        Long memberId
) implements Serializable {}
