package com.lemnt.trade.adapter.web.brokerage.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 管理端提现单。 */
@Schema(description = "管理端提现单")
public record BrokerageWithdrawRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "会员编号") Long userId,
        @Schema(description = "提现金额，元") BigDecimal price,
        @Schema(description = "手续费，元") BigDecimal feePrice,
        @Schema(description = "当前总佣金，元") BigDecimal totalPrice,
        @Schema(description = "提现方式") Integer type,
        @Schema(description = "收款人") String userName,
        @Schema(description = "收款账号") String userAccount,
        @Schema(description = "收款码") String qrCodeUrl,
        @Schema(description = "银行名称") String bankName,
        @Schema(description = "开户地址") String bankAddress,
        @Schema(description = "状态") Integer status,
        @Schema(description = "审核说明") String auditReason,
        @Schema(description = "审核时间") LocalDateTime auditTime,
        @Schema(description = "创建时间") LocalDateTime createTime
) implements java.io.Serializable {
}
