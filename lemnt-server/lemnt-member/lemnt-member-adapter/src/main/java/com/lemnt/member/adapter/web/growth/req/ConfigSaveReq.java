package com.lemnt.member.adapter.web.growth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 配置保存请求。
 */
@Schema(description = "ConfigSave请求")
public record ConfigSaveReq(
        @Schema(description = "积分抵扣及按支付金额赠送积分开关")
        @NotNull
        Boolean pointTradeDeductEnable,
        @Schema(description = "每积分抵扣金额，分")
        @NotNull
        Integer pointTradeDeductUnitPrice,
        @Schema(description = "每单抵扣积分上限，0不限")
        @NotNull
        Integer pointTradeDeductMaxPrice,
        @Schema(description = "每元赠送积分数")
        @NotNull
        Integer pointTradeGivePoint
) implements Serializable {}
