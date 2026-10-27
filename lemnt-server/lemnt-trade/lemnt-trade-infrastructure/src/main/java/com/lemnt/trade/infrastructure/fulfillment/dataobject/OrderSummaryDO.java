package com.lemnt.trade.infrastructure.fulfillment.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单汇总数据对象。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class OrderSummaryDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String orderNo;
    private Long memberId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createTime;
    private Boolean inventoryConfirmed;
    private Boolean activeRefund;
    private String carrierCode;
    private String trackingNo;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
}
