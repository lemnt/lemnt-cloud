package com.lemnt.member.application.growth.qry;

import java.io.Serializable;

/**
 * 查询等级查询。
 */
public record LevelListQry(
        boolean enabledOnly
) implements Serializable {}
