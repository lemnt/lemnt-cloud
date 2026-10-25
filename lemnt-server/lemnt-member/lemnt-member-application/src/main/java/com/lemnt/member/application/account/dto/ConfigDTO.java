package com.lemnt.member.application.account.dto;

public record ConfigDTO(
        Boolean pointTradeDeductEnable,
        Integer pointTradeDeductUnitPrice,
        Integer pointTradeDeductMaxPrice,
        Integer pointTradeGivePoint) {}
