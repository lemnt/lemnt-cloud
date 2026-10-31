package com.lemnt.member.application.growth.qry;

import java.io.Serializable;

/**
 * 签到配置项查询。
 */
public record SignConfigListQry(
        boolean enabledOnly
) implements Serializable {}
