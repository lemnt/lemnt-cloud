package com.lemnt.order.client.event;

import com.lemnt.order.client.dto.OrderStockLineDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单服务校验后产生的冻结业务事实，不包含当前配置及个人敏感资料。
 */
public record OrderPaidEvent(
    String orderNo,
    Long memberId,
    BigDecimal amount,
    long usePoint,
    long givePoint,
    Map<Long, Integer> giveCouponTemplateCounts,
    List<OrderStockLineDTO> items)
    implements Serializable {
}
