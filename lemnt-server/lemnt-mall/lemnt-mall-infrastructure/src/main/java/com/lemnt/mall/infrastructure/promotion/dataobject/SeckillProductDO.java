package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 秒杀活动商品持久化对象。 */
@Data
@TableName("mall_promotion_seckill_product")
public class SeckillProductDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long activityId;
    private String configIds;
    private Long spuId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer stock;
    private Integer activityStatus;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
}
