package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/** mall_promotion_scope 表数据对象，联合主键通过条件构造器操作。 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_promotion_scope")
public class PromotionScopeDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 促销主记录编号。 */
    private Long rootId;

    /** 版本号。 */
    private Integer version;

    /** 范围类型。 */
    private String type;

    /** 目标编号。 */
    private Long targetId;
}
