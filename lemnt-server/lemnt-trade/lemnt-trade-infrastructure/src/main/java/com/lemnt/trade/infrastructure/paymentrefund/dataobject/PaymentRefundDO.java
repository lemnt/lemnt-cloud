package com.lemnt.trade.infrastructure.paymentrefund.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/** 支付退款交易表。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("trade_payment_refund")
public class PaymentRefundDO extends BaseDO {
    @TableId(type = IdType.INPUT)
    private String refundNo;
    private String afterSaleNo;
    private String paymentNo;
    private String orderNo;
    private String channel;
    private BigDecimal amount;
    private String status;
    private String channelRefundNo;
}
