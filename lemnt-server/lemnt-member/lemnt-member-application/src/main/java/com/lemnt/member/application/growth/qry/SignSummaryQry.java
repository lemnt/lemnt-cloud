package com.lemnt.member.application.growth.qry;

import java.io.Serializable;

/**
 * 签到汇总查询。
 */
public record SignSummaryQry(
        Long memberId
) implements Serializable {}
