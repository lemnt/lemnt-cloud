package com.lemnt.member.application.growth.cmd;

import java.io.Serializable;

/**
 * 配置保存命令。
 */
public record ConfigSaveCmd(
        Boolean pointTradeDeductEnable,
        Integer pointTradeDeductUnitPrice,
        Integer pointTradeDeductMaxPrice,
        Integer pointTradeGivePoint
) implements Serializable {}
