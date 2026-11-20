package com.lemnt.pay.infrastructure.payment.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/** 支付主单只保存业务归属、金额和首次确认的收款尝试。 */
@Data
@Accessors(chain = true)
@TableName("pay_payment_order")
public class PaymentOrderDO {
    @TableId private String orderNo;
    private String paymentOrderNo;
    private Long memberId;
    private BigDecimal amount;
    private String status;
    private String paidPaymentNo;
}
