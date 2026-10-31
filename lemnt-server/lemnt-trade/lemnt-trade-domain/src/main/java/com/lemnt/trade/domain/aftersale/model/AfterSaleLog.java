package com.lemnt.trade.domain.aftersale.model;

import java.time.LocalDateTime;

/** 对齐芋道售后操作日志的业务字段。 */
public record AfterSaleLog(Long id, Long userId, Integer userType, Long afterSaleId, Integer beforeStatus,
        Integer afterStatus, Integer operateType, String content, LocalDateTime createTime) {
}
