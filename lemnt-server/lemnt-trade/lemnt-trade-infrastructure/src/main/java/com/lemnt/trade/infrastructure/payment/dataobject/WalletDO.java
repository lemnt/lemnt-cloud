package com.lemnt.trade.infrastructure.payment.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** trade_member_wallet 数据对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_member_wallet")
public class WalletDO extends BaseDO {
    /** 钱包编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会员编号，唯一。 */
    private Long memberId;

    /** 可用余额，元。 */
    private BigDecimal balance;

    /** 冻结金额，元。 */
    private BigDecimal freezePrice;

    /** 累计支出，元。 */
    private BigDecimal totalExpense;

    /** 累计充值，元。 */
    private BigDecimal totalRecharge;
}
