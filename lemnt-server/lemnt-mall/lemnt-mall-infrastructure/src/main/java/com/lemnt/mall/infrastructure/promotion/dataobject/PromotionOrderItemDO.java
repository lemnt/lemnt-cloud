package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/** 活动订单商品行持久化对象。 */
@Data
@TableName("mall_promotion_order_item")
public class PromotionOrderItemDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String orderNo;
    private Long skuId;
    private Integer quantity;
    private Long usePoint;
    private Integer refundedQuantity;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
