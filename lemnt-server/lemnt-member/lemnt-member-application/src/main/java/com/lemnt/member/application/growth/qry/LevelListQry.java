package com.lemnt.member.application.growth.qry;

import java.io.Serializable;

/**
 * 查询等级查询。
 */
public record LevelListQry(
        boolean enabledOnly,
        String name,
        Integer status
) implements Serializable {
    /** 既有简单列表与App调用不附加后台条件。 */
    public LevelListQry(boolean enabledOnly) { this(enabledOnly, null, null); }
}
