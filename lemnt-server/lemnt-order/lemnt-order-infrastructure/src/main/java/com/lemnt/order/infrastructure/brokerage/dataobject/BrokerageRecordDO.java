package com.lemnt.order.infrastructure.brokerage.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 佣金记录持久化对象。
 */
@Data
@TableName("order_brokerage_record")
public class BrokerageRecordDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String bizId;
    private Integer bizType;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private Integer status;
    private Integer frozenDays;
    private LocalDateTime unfreezeTime;
    private Integer sourceUserLevel;
    private Long sourceUserId;
    private LocalDateTime createTime;
}
