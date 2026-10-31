package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;

/** 活动业务表持久化对象。 */
@Data
@TableName("mall_promotion_discount_product")
public class DiscountProductDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long activityId;
    private Long spuId;
    private Long skuId;
    private String discountType;
    private Integer discountPercent;
    private BigDecimal discountPrice;
}
