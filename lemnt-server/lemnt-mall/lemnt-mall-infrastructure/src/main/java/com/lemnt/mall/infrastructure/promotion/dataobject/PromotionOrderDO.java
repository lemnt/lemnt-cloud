package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 活动订单映射持久化对象。 */
@Data
@TableName("mall_promotion_order")
public class PromotionOrderDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String orderNo;
    private String requestNo;
    private Long memberId;
    private String activityType;
    private Long activityId;
    private Long activityRecordId;
    private Long headId;
    private Long skuId;
    private Integer quantity;
    private BigDecimal activityPrice;
    private Long usePoint;
    private Integer refundedQuantity;
    private String status;
    private String failureReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
