package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "ConfigSave请求")
public record ConfigSaveReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "积分抵扣开关，交易集成另行接入")
                @jakarta.validation.constraints.NotNull
                Boolean pointTradeDeductEnable,
        @io.swagger.v3.oas.annotations.media.Schema(description = "每积分抵扣金额，分")
                @jakarta.validation.constraints.NotNull
                Integer pointTradeDeductUnitPrice,
        @io.swagger.v3.oas.annotations.media.Schema(description = "积分抵扣金额上限，分")
                @jakarta.validation.constraints.NotNull
                Integer pointTradeDeductMaxPrice,
        @io.swagger.v3.oas.annotations.media.Schema(description = "每元赠送积分数")
                @jakarta.validation.constraints.NotNull
                Integer pointTradeGivePoint)
        implements java.io.Serializable {}
