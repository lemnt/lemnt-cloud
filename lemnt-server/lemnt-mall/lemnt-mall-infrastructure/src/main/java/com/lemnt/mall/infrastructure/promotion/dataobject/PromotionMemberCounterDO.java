package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/** mall_promotion_member_counter 表数据对象，联合主键通过条件构造器操作。 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_promotion_member_counter")
public class PromotionMemberCounterDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 促销主记录编号。 */
    private Long rootId;

    /** 会员编号。 */
    private Long memberId;

    /** 占用次数。 */
    private Long heldCount;

    /** 已使用次数。 */
    private Long usedCount;
}
