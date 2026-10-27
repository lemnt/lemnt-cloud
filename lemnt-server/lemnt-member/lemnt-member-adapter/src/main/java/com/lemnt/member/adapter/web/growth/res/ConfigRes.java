package com.lemnt.member.adapter.web.growth.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

/**
 * 配置响应。
 */
@Schema(description = "会员Config响应")
public record ConfigRes(
        @Schema(description = "积分抵扣开关，交易集成另行接入")
        Boolean pointTradeDeductEnable,
        @Schema(description = "每积分抵扣金额，分")
        Integer pointTradeDeductUnitPrice,
        @Schema(description = "积分抵扣金额上限，分")
        Integer pointTradeDeductMaxPrice,
        @Schema(description = "每元赠送积分数")
        Integer pointTradeGivePoint
) implements Serializable {}
