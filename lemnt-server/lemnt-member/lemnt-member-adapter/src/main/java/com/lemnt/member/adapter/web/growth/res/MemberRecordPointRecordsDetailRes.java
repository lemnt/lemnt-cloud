package com.lemnt.member.adapter.web.growth.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资产记录响应。
 */
@Schema(description = "会员AssetRecord响应")
public record MemberRecordPointRecordsDetailRes(
        @Schema(description = "记录编号")
        Long id,
        @Schema(description = "会员编号")
        Long memberId,
        @Schema(description = "资产：POINT积分，EXPERIENCE经验")
        String assetType,
        @Schema(description = "本次有符号变更量")
        Long delta,
        @Schema(description = "变更后余额")
        Long balance,
        @Schema(description = "业务类型")
        String bizType,
        @Schema(description = "幂等业务编号")
        String bizNo,
        @Schema(description = "原因")
        String reason,
        @Schema(description = "数据库时间")
        LocalDateTime createTime
) implements Serializable {}
