package com.lemnt.trade.domain.payment.model;

import java.time.LocalDateTime;

/** 支付应用：一个商户可配置多个应用，渠道挂在应用下。状态 0 启用、1 停用。 */
public record PayApp(
        Long id,
        String appKey,
        String name,
        Integer status,
        String remark,
        String orderNotifyUrl,
        String refundNotifyUrl,
        LocalDateTime createTime
) {
}
