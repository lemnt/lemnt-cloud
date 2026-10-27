package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/** mall_promotion_usage_event 表数据对象。 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_promotion_usage_event")
public class PromotionUsageEventDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 变更前状态。 */
    private String fromState;

    /** 订单号。 */
    private String orderNo;

    /** 促销主记录编号。 */
    private Long rootId;

    /** 目标状态。 */
    private String targetState;

    /** 变更原因。 */
    private String reason;

    /** 发生时间，毫秒时间戳。 */
    private Long occurredAtEpochMs;
}
