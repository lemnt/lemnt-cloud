package com.lemnt.trade.adapter.web.wallet.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 钱包转账响应。 */
@Schema(description = "钱包转账响应")
public record WalletTransferRes(
        @Schema(description = "转账编号")
        Long id,
        @Schema(description = "转账单号")
        String transferNo,
        @Schema(description = "请求编号")
        String requestNo,
        @Schema(description = "转出钱包编号")
        Long fromWalletId,
        @Schema(description = "转出会员编号")
        Long fromMemberId,
        @Schema(description = "转入钱包编号")
        Long toWalletId,
        @Schema(description = "转入会员编号")
        Long toMemberId,
        @Schema(description = "转账金额，元")
        BigDecimal amount,
        @Schema(description = "备注")
        String remark,
        @Schema(description = "状态")
        String status,
        @Schema(description = "创建时间")
        LocalDateTime createTime
) implements Serializable {
}
