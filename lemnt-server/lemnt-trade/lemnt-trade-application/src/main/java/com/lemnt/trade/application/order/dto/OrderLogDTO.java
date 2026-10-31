package com.lemnt.trade.application.order.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 订单状态日志数据。 */
public record OrderLogDTO(Long id, Long userId, Integer userType, Long orderId, Integer beforeStatus,
        Integer afterStatus, Integer operateType, String content, LocalDateTime createTime) implements Serializable {
}
