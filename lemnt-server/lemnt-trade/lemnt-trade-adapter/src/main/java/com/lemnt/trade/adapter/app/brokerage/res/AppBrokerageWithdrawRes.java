package com.lemnt.trade.adapter.app.brokerage.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 会员提现单。 */
@Schema(description = "会员提现单")
public record AppBrokerageWithdrawRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "提现金额，元") BigDecimal price,
        @Schema(description = "手续费，元") BigDecimal feePrice,
        @Schema(description = "当前总佣金，元") BigDecimal totalPrice,
        @Schema(description = "提现方式") Integer type,
        @Schema(description = "状态") Integer status,
        @Schema(description = "驳回原因") String auditReason,
        @Schema(description = "审核时间") LocalDateTime auditTime,
        @Schema(description = "创建时间") LocalDateTime createTime)
        implements java.io.Serializable {}
