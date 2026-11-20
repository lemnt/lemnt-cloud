package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Schema(description = "会员统计查询")
public record MemberStatisticsReq(
        @NotNull
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                @Schema(description = "注册趋势开始日期，包含当天", example = "2026-11-01")
                LocalDate from,
        @NotNull
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                @Schema(description = "注册趋势结束日期，包含当天", example = "2026-11-18")
                LocalDate to)
        implements Serializable {}
