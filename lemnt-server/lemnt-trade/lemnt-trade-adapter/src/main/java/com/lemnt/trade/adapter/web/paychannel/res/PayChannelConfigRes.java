package com.lemnt.trade.adapter.web.paychannel.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 支付渠道配置响应，不返回配置明文，只返回是否已配置。 */
@Schema(description = "支付渠道配置响应")
public record PayChannelConfigRes(
        @Schema(description = "渠道配置编号")
        Long id,
        @Schema(description = "所属支付应用编号")
        Long appId,
        @Schema(description = "渠道编码")
        String code,
        @Schema(description = "状态：0启用、1停用")
        Integer status,
        @Schema(description = "渠道费率百分比")
        BigDecimal feeRate,
        @Schema(description = "备注")
        String remark,
        @Schema(description = "是否已保存渠道配置")
        boolean configConfigured,
        @Schema(description = "创建时间")
        LocalDateTime createTime
) implements Serializable {
}
