package com.lemnt.order.application.brokerage.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 佣金记录输出。 */
public record BrokerageRecordDTO(
        Long id,
        Long userId,
        String bizId,
        Integer bizType,
        String title,
        String description,
        BigDecimal price,
        BigDecimal totalPrice,
        Integer status,
        Integer frozenDays,
        LocalDateTime unfreezeTime,
        Integer sourceUserLevel,
        LocalDateTime createTime)
        implements Serializable {}
