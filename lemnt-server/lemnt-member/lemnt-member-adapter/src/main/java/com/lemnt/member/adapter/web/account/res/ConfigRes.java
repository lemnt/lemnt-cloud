package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员Config响应")
public record ConfigRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "积分抵扣开关，交易集成另行接入")
                Boolean pointTradeDeductEnable,
        @io.swagger.v3.oas.annotations.media.Schema(description = "每积分抵扣金额，分")
                Integer pointTradeDeductUnitPrice,
        @io.swagger.v3.oas.annotations.media.Schema(description = "积分抵扣金额上限，分")
                Integer pointTradeDeductMaxPrice,
        @io.swagger.v3.oas.annotations.media.Schema(description = "每元赠送积分数")
                Integer pointTradeGivePoint)
        implements java.io.Serializable {}
