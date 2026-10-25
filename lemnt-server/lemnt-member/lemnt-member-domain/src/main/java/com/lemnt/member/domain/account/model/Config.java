package com.lemnt.member.domain.account.model;

public record Config(
        Boolean pointTradeDeductEnable,
        Integer pointTradeDeductUnitPrice,
        Integer pointTradeDeductMaxPrice,
        Integer pointTradeGivePoint) {}
