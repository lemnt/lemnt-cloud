package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 拼团记录持久化对象。 */
@Data
@TableName("mall_promotion_combination_record")
public class CombinationRecordDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long activityId;
    private BigDecimal combinationPrice;
    private Long spuId;
    private String spuName;
    private String picUrl;
    private Long skuId;
    private Integer count;
    private Long userId;
    private String nickname;
    private String avatar;
    private Long headId;
    private Integer status;
    private Long orderId;
    private Integer userSize;
    private Integer userCount;
    private Boolean virtualGroup;
    private LocalDateTime expireTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
