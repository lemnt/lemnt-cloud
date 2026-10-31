package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 拼团活动商品持久化对象。 */
@Data
@TableName("mall_promotion_combination_product")
public class CombinationProductDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long activityId;
    private Long spuId;
    private Long skuId;
    private BigDecimal combinationPrice;
    private Integer activityStatus;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
}
