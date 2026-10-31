package com.lemnt.trade.infrastructure.payment.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** trade_pay_recharge_package 数据对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_pay_recharge_package")
public class RechargePackageDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal payPrice;
    private BigDecimal bonusPrice;
    private Integer status;
    @TableLogic(value = "0", delval = "1")
    private Boolean deleted;
}
