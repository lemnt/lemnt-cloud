package com.lemnt.trade.infrastructure.promotion.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/** 生成售后时冻结的优惠回退归因持久化对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("trade_order_promotion_refund")
public class OrderPromotionRefundDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    /** 主键编号。 */
    private Long id;

    /** 本次售后主键。 */
    private Long afterSaleId;

    /** 原行优惠主键。 */
    private Long orderItemPromotionId;

    /** 本次商品优惠归因，单位元，不额外增加现金退款。 */
    private BigDecimal refundDiscountAmount;

    /** 本次运费优惠归因，单位元，不额外增加现金退款。 */
    private BigDecimal refundDeliveryDiscountPrice;

    /** 生成退单时冻结的应返使用积分，到账以账户流水为准。 */
    private Long returnUsePoint;

    /** 生成退单时冻结的应回收赠送积分。 */
    private Long deductGivePoint;
}
