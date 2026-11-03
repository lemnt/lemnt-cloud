package com.lemnt.pay.infrastructure.payment.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** pay_wallet_recharge 数据对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_wallet_recharge")
public class WalletRechargeDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String rechargeNo;
    private String requestNo;
    private Long walletId;
    private Long memberId;
    private Long packageId;
    private BigDecimal payPrice;
    private BigDecimal bonusPrice;
    private BigDecimal totalPrice;
    private String status;
    private String channelCode;
    private LocalDateTime paidTime;
    private LocalDateTime refundedTime;
}
