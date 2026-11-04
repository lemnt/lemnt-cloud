package com.lemnt.pay.infrastructure.payment.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易支付数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pay_payment")
public class TradePaymentDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String paymentNo;
    private String orderNo;
    private Long memberId;
    private String channel;
    private String appKey;
    private String channelTradeNo;
    private BigDecimal amount;
    private String status;
    private LocalDateTime paidTime;
    private String notifyId;
}
