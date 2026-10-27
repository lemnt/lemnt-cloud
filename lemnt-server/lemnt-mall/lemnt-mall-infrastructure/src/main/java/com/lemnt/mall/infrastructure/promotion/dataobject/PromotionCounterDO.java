package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 营销计数器数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_promotion_counter")
public class PromotionCounterDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 表主键。 */
    @TableId(type = IdType.INPUT)
    private Long rootId;

    private Long heldCount;
    private Long usedCount;
    private Long globalLimit;
    private Long memberLimit;
}
