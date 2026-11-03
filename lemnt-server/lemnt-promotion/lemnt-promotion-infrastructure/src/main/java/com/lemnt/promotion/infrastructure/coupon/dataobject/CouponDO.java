package com.lemnt.promotion.infrastructure.coupon.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("promotion_coupon")
public class CouponDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;
    private Long memberId;
    private String status;
    private String orderNo;

    /** 营销活动赠券来源订单，普通持券为空。 */
    private String sourceOrderNo;

    /** 同一来源订单、模板和会员下的赠券发放序号。 */
    private Integer giftSequence;

    /** code。 */
    private String code;

    /** name。 */
    private String name;

    /** takeType。 */
    private String takeType;

    /** kind。 */
    private String kind;

    /** threshold。 */
    private BigDecimal threshold;

    /** fixedAmount。 */
    private BigDecimal fixedAmount;

    /** discountPercent。 */
    private Integer discountPercent;

    /** discountLimit。 */
    private BigDecimal discountLimit;

    /** 适用商品范围。 */
    private String productScope;

    /** 适用SPU或分类编号JSON。 */
    private String productScopeValues;

    /** validFrom。 */
    private LocalDateTime validFrom;

    /** validUntil。 */
    private LocalDateTime validUntil;

    /** useTime。 */
    private LocalDateTime useTime;

    /** 逻辑删除，保留已发券历史。 */
    @TableLogic(value = "0", delval = "1")
    private Boolean deleted;
}
