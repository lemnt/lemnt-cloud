package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;

/** 积分商城活动商品持久化对象。 */
@Data
@TableName("mall_promotion_point_product")
public class PointProductDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long activityId;
    private Long spuId;
    private Long skuId;
    private Integer exchangeCount;
    private Integer point;
    private BigDecimal price;
    private Integer stock;
    private Integer activityStatus;
}
