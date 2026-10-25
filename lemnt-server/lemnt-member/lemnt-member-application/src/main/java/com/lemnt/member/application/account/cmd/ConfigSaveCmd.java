package com.lemnt.member.application.account.cmd;

public record ConfigSaveCmd(
        Boolean pointTradeDeductEnable,
        Integer pointTradeDeductUnitPrice,
        Integer pointTradeDeductMaxPrice,
        Integer pointTradeGivePoint) {}
