package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 砍价记录持久化对象。 */
@Data
@TableName("mall_promotion_bargain_record")
public class BargainRecordDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long activityId;
    private Long spuId;
    private Long skuId;
    private BigDecimal bargainFirstPrice;
    private BigDecimal bargainPrice;
    private Integer status;
    private LocalDateTime endTime;
    private Long orderId;
}
