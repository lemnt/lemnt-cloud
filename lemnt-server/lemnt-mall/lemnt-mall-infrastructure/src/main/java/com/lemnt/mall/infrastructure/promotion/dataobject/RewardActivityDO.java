package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/** 活动业务表持久化对象。 */
@Data
@TableName("mall_promotion_reward_activity")
public class RewardActivityDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;
    private String conditionType;
    private String productScope;
    private String productScopeValuesJson;
    private String rulesJson;
}
