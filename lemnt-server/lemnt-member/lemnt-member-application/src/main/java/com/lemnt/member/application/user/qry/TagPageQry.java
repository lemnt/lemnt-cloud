package com.lemnt.member.application.user.qry;

import java.io.Serializable;

/** 标签分页查询。 */
public record TagPageQry(
        String name,
        int pageNum,
        int pageSize
) implements Serializable {}
