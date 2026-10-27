package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 营销使用数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_promotion_usage")
public class PromotionUsageDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String orderNo;
    private Long rootId;
    private Integer version;
    private Long memberId;
    private Long discountFen;
    private String ruleHash;
    private String state;

    /** 创建时间，毫秒时间戳。 */
    private Long createdAtEpochMs;

    /** 更新时间，毫秒时间戳。 */
    private Long updatedAtEpochMs;
}
