package com.lemnt.order.infrastructure.promotion.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/** 订单实际采用的优惠与权益结果持久化对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("order_promotion")
public class OrderPromotionDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    /** 主键编号。 */
    private Long id;

    /** 原订单主键。 */
    private Long orderId;

    /** 订单内稳定优惠标识，重复请求必须保持一致。 */
    private String promotionKey;

    /** 已生效优惠来源类型。 */
    private String promotionType;

    /** 来源活动、会员券或等级编号，无可确认来源时为NULL。 */
    private Long sourceId;

    /** 下单时优惠展示名称快照，不读取现值回填。 */
    private String promotionName;

    /** 下单时优惠规则展示摘要。 */
    private String ruleSummary;

    /** 实际采用的计算因子JSON快照，供核对和支付前调价使用。 */
    private String ruleSnapshot;

    /** 商品优惠金额，单位元，历史未知为NULL。 */
    private BigDecimal discountAmount;

    /** 运费优惠金额，单位元，历史未知为NULL。 */
    private BigDecimal deliveryDiscountPrice;

    /** 使用积分数量，历史未知为NULL。 */
    private Long usePoint;

    /** 赠送积分数量，历史未知为NULL。 */
    private Long givePoint;

    /** 该优惠来源赠券模板与数量JSON，历史未知为NULL。 */
    private String giveCouponTemplateCounts;
}
