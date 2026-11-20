package com.lemnt.pay.application.payment.cmd;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 按套餐或自定义金额创建充值单。
 */
public record WalletRechargeCreateCmd(Long packageId, BigDecimal amount, String requestNo)
        implements Serializable {}
