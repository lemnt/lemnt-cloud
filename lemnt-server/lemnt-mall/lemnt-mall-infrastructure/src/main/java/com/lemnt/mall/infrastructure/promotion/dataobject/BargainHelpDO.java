package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;

/** 砍价助力持久化对象。 */
@Data
@TableName("mall_promotion_bargain_help")
public class BargainHelpDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long activityId;
    private Long recordId;
    private Long userId;
    private BigDecimal reducePrice;
}
