package com.lemnt.member.application.user.qry;

import java.time.LocalDate;

/** 注册趋势的起止日期，均包含。 */
public record MemberStatisticsQry(LocalDate from, LocalDate to) {}
