package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 秒杀时段持久化对象。 */
@Data
@TableName("mall_promotion_seckill_config")
public class SeckillConfigDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private String startTime;
    private String endTime;
    private String sliderPicUrls;
    private Integer status;
}
