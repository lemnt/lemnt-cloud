package com.lemnt.order.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动订单退款去重持久化对象。
 */
@Data
@TableName("order_activity_refund")
public class PromotionOrderRefundDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String orderNo;
    private String refundNo;
    private Integer quantity;
    private LocalDateTime createTime;
}
