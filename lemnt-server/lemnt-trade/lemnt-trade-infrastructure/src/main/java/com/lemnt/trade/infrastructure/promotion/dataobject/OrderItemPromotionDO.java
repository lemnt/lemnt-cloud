package com.lemnt.trade.infrastructure.promotion.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/** 优惠在原订单商品明细上的分摊持久化对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("trade_order_item_promotion")
public class OrderItemPromotionDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    /** 主键编号。 */
    private Long id;

    /** 原优惠主键。 */
    private Long orderPromotionId;

    /** 原订单明细主键。 */
    private Long orderItemId;

    /** 商品优惠金额，单位元，历史未知为NULL。 */
    private java.math.BigDecimal discountAmount;

    /** 运费优惠金额，单位元，历史未知为NULL。 */
    private java.math.BigDecimal deliveryDiscountPrice;

    /** 使用积分数量，历史未知为NULL。 */
    private Long usePoint;

    /** 赠送积分数量，历史未知为NULL。 */
    private Long givePoint;
}
