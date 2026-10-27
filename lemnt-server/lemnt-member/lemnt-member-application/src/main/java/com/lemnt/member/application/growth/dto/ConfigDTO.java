package com.lemnt.member.application.growth.dto;

import java.io.Serializable;

/**
 * 配置数据传输对象。
 */
public record ConfigDTO(
        Boolean pointTradeDeductEnable,
        Integer pointTradeDeductUnitPrice,
        Integer pointTradeDeductMaxPrice,
        Integer pointTradeGivePoint
) implements Serializable {}
