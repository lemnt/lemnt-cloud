package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 积分商城活动持久化对象。 */
@Data
@TableName("mall_promotion_point_activity")
public class PointActivityDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long spuId;
    private Integer status;
    private String remark;
    private Integer sort;
    private Integer stock;
    private Integer totalStock;
}
