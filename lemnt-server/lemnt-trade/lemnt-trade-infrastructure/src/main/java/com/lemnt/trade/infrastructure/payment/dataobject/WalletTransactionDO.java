package com.lemnt.trade.infrastructure.payment.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** trade_member_wallet_transaction 数据对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_member_wallet_transaction")
public class WalletTransactionDO extends BaseDO {
    /** 流水编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 流水号。 */
    private String transactionNo;

    /** 钱包编号。 */
    private Long walletId;

    /** 业务类型。 */
    private String bizType;

    /** 业务单号。 */
    private String bizNo;

    /** 流水说明。 */
    private String title;

    /** 带符号金额，正增负减，元。 */
    private BigDecimal amount;

    /** 交易后可用余额，元。 */
    private BigDecimal balanceAfter;
}
