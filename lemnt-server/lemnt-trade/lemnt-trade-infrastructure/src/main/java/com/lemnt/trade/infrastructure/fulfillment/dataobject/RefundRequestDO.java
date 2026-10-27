package com.lemnt.trade.infrastructure.fulfillment.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款请求数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("trade_refund_request")
public class RefundRequestDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String refundNo;
    private String orderNo;
    private String requestNo;
    private Long memberId;
    private String paymentNo;
    private BigDecimal amount;
    private String reason;
    private String status;
    private String decisionReason;
    private LocalDateTime requestedAt;
    private LocalDateTime decidedAt;

    /** 正在退款的订单号。 */
    private String activeOrderNo;
}
