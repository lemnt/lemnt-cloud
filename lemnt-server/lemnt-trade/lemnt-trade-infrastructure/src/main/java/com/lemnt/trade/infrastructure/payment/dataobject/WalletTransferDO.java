package com.lemnt.trade.infrastructure.payment.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** trade_member_wallet_transfer 数据对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_member_wallet_transfer")
public class WalletTransferDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String transferNo;
    private String requestNo;
    private Long fromWalletId;
    private Long fromMemberId;
    private Long toWalletId;
    private Long toMemberId;
    private BigDecimal amount;
    private String remark;
    private String status;
}
