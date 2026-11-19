package com.lemnt.order.infrastructure.brokerage.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/** 佣金行配置和入账快照。 */
@Data
@Accessors(chain = true)
@TableName("order_brokerage_line")
public class OrderCommissionLineDO {
    @TableId(type = IdType.INPUT)
    private Long orderItemId;

    private Long orderId;
    private BigDecimal firstFixed;
    private BigDecimal secondFixed;
    private BigDecimal firstEarned;
    private BigDecimal secondEarned;
}
