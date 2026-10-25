package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员LevelRecord响应")
public record LevelRecordRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "记录编号") Long id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "会员编号") Long memberId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "等级编号，可空") Long levelId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "经验值或等级升级门槛") Long experience,
        @io.swagger.v3.oas.annotations.media.Schema(description = "原因") String reason,
        @io.swagger.v3.oas.annotations.media.Schema(description = "数据库时间")
                java.time.LocalDateTime createTime)
        implements java.io.Serializable {}
