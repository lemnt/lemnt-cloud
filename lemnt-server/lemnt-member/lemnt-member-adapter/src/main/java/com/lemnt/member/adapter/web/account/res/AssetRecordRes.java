package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员AssetRecord响应")
public record AssetRecordRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "记录编号") Long id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "会员编号") Long memberId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "资产：POINT积分，EXPERIENCE经验")
                String assetType,
        @io.swagger.v3.oas.annotations.media.Schema(description = "本次有符号变更量") Long delta,
        @io.swagger.v3.oas.annotations.media.Schema(description = "变更后余额") Long balance,
        @io.swagger.v3.oas.annotations.media.Schema(description = "业务类型") String bizType,
        @io.swagger.v3.oas.annotations.media.Schema(description = "幂等业务编号") String bizNo,
        @io.swagger.v3.oas.annotations.media.Schema(description = "原因") String reason,
        @io.swagger.v3.oas.annotations.media.Schema(description = "数据库时间")
                java.time.LocalDateTime createTime)
        implements java.io.Serializable {}
