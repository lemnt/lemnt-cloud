package com.lemnt.trade.adapter.web.brokerage.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 管理端佣金记录。 */
@Schema(description = "管理端佣金记录")
public record BrokerageRecordRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "会员编号") Long userId,
        @Schema(description = "业务编号") String bizId,
        @Schema(description = "业务类型") Integer bizType,
        @Schema(description = "标题") String title,
        @Schema(description = "说明") String description,
        @Schema(description = "佣金金额，元") BigDecimal price,
        @Schema(description = "当前总佣金，元") BigDecimal totalPrice,
        @Schema(description = "状态") Integer status,
        @Schema(description = "冻结天数") Integer frozenDays,
        @Schema(description = "解冻时间") LocalDateTime unfreezeTime,
        @Schema(description = "来源层级") Integer sourceUserLevel,
        @Schema(description = "创建时间") LocalDateTime createTime
) implements java.io.Serializable {
}
