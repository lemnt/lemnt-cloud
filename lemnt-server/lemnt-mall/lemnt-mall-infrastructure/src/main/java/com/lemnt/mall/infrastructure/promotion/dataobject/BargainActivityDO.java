package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 砍价活动持久化对象。 */
@Data
@TableName("mall_promotion_bargain_activity")
public class BargainActivityDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Long spuId;
    private Long skuId;
    private BigDecimal bargainFirstPrice;
    private BigDecimal bargainMinPrice;
    private Integer stock;
    private Integer totalStock;
    private Integer helpMaxCount;
    private Integer bargainCount;
    private Integer totalLimitCount;
    private BigDecimal randomMinPrice;
    private BigDecimal randomMaxPrice;
}
