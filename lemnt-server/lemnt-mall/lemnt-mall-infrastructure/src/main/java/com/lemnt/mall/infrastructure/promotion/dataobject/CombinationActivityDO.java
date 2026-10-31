package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/** 拼团活动持久化对象。 */
@Data
@TableName("mall_promotion_combination_activity")
public class CombinationActivityDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private Long spuId;
    private Integer totalLimitCount;
    private Integer singleLimitCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer userSize;
    private Boolean virtualGroup;
    private Integer status;
    private Integer limitDuration;
}
