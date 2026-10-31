package com.lemnt.trade.application.payment.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 支付应用应用输出。 */
public record PayAppDTO(
        Long id,
        String appKey,
        String name,
        Integer status,
        String remark,
        String orderNotifyUrl,
        String refundNotifyUrl,
        LocalDateTime createTime
) implements Serializable {
}
