package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/** 秒杀活动持久化对象。 */
@Data
@TableName("mall_promotion_seckill_activity")
public class SeckillActivityDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long spuId;
    private String name;
    private Integer status;
    private String remark;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer sort;
    private String configIds;
    private Integer totalLimitCount;
    private Integer singleLimitCount;
    private Integer stock;
    private Integer totalStock;
}
