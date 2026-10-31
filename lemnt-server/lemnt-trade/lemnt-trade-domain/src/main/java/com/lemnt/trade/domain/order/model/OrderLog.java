package com.lemnt.trade.domain.order.model;

import java.time.LocalDateTime;

/** 对齐芋道订单操作日志的业务字段。 */
public record OrderLog(Long id, Long userId, Integer userType, Long orderId, Integer beforeStatus,
        Integer afterStatus, Integer operateType, String content, LocalDateTime createTime) {
}
