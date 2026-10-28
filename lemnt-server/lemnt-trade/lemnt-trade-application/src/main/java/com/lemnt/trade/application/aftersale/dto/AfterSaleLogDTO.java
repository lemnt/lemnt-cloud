package com.lemnt.trade.application.aftersale.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 售后状态日志数据。 */
public record AfterSaleLogDTO(Long id, Long userId, Integer userType, Long afterSaleId, Integer beforeStatus,
        Integer afterStatus, Integer operateType, String content, LocalDateTime createTime) implements Serializable {
}
