package com.lemnt.trade.adapter.web.wallet.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 钱包账户响应。 */
@Schema(description = "钱包账户响应")
public record WalletRes(
        @Schema(description = "钱包编号")
        Long id,
        @Schema(description = "会员编号")
        Long memberId,
        @Schema(description = "可用余额，元")
        BigDecimal balance,
        @Schema(description = "冻结金额，元")
        BigDecimal freezePrice,
        @Schema(description = "累计支出，元")
        BigDecimal totalExpense,
        @Schema(description = "累计充值，元")
        BigDecimal totalRecharge,
        @Schema(description = "创建时间")
        LocalDateTime createTime
) implements Serializable {
}
