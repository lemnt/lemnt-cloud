package com.lemnt.member.domain.growth.model;

/**
 * 配置。
 */
public record Config(
        Boolean pointTradeDeductEnable,
        Integer pointTradeDeductUnitPrice,
        Integer pointTradeDeductMaxPrice,
        Integer pointTradeGivePoint
) {}
