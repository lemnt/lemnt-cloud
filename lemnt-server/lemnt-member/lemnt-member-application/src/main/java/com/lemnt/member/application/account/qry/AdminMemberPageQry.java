package com.lemnt.member.application.account.qry;

public record AdminMemberPageQry(
        String kind, Long memberId, String name, Integer status, int pageNum, int pageSize) {}
