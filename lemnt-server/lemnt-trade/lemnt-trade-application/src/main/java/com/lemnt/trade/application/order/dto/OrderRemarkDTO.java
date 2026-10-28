package com.lemnt.trade.application.order.dto;

import java.io.Serializable;

/** 订单备注查询结果。 */
public record OrderRemarkDTO(
        String remark
) implements Serializable {
}
