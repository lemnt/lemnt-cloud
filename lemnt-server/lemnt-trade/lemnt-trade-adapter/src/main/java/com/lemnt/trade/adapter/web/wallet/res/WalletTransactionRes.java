package com.lemnt.trade.adapter.web.wallet.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 钱包流水响应。 */
@Schema(description = "钱包流水响应")
public record WalletTransactionRes(
        @Schema(description = "流水号")
        String transactionNo,
        @Schema(description = "业务类型")
        String bizType,
        @Schema(description = "业务单号")
        String bizNo,
        @Schema(description = "流水说明")
        String title,
        @Schema(description = "带符号金额，正增负减，元")
        BigDecimal amount,
        @Schema(description = "交易后可用余额，元")
        BigDecimal balanceAfter,
        @Schema(description = "创建时间")
        LocalDateTime createTime
) implements Serializable {
}
