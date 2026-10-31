package com.lemnt.member.adapter.web.growth.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

/**
 * 配置响应。
 */
@Schema(description = "会员Config响应")
public record ConfigRes(
        @Schema(description = "积分抵扣及按支付金额赠送积分开关")
        Boolean pointTradeDeductEnable,
        @Schema(description = "每积分抵扣金额，分")
        Integer pointTradeDeductUnitPrice,
        @Schema(description = "每单抵扣积分上限，0不限")
        Integer pointTradeDeductMaxPrice,
        @Schema(description = "每元赠送积分数")
        Integer pointTradeGivePoint
) implements Serializable {}
