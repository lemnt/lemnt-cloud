package com.lemnt.order.infrastructure.brokerage.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分销用户持久化对象。
 */
@Data
@TableName("order_brokerage_user")
public class BrokerageUserDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private Long bindUserId;
    private LocalDateTime bindUserTime;
    private Boolean brokerageEnabled;
    private LocalDateTime brokerageTime;
    private BigDecimal brokeragePrice;
    private BigDecimal frozenPrice;
}
