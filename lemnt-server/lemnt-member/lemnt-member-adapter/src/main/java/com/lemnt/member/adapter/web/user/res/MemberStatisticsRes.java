package com.lemnt.member.adapter.web.user.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

@Schema(description = "会员注册及档案分布")
public record MemberStatisticsRes(
        @Schema(description = "按日期升序的日新增会员数") List<Registration> registrations,
        @Schema(description = "当前全部会员性别分布") List<Distribution> sexes,
        @Schema(description = "当前全部会员档案地区分布") List<Distribution> areas)
        implements Serializable {

    @Schema(description = "日新增注册人数")
    public record Registration(
            @Schema(description = "会员创建日期，YYYY-MM-DD") String date,
            @Schema(description = "会员人数") long count)
            implements Serializable {}

    @Schema(description = "会员档案分布及人数")
    public record Distribution(
            @Schema(description = "地区编号可空；性别0未知、1男、2女") Integer value,
            @Schema(description = "会员人数") long count)
            implements Serializable {}
}
