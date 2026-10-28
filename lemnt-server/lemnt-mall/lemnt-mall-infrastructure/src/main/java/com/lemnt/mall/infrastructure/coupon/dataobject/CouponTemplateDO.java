package com.lemnt.mall.infrastructure.coupon.dataobject;

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
 * 优惠券模板数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mall_coupon_template")
public class CouponTemplateDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String kind;
    private BigDecimal threshold;
    private BigDecimal fixedAmount;
    private Integer discountPercent;
    private BigDecimal discountLimit;
    private Integer totalCount;
    private Integer issuedCount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean enabled;
    /** 领取方式：USER 会员领取，ADMIN 后台发放，REGISTER 注册赠送。 */
    private String takeType;

    /** 使用说明。 */
    private String description;
    /** 每人限领数量，-1不限。 */
    private Integer takeLimitCount;
    /** 适用商品范围。 */
    private String productScope;
    /** 适用SPU或分类编号JSON。 */
    private String productScopeValues;
    /** 有效期类型。 */
    private String validityType;
    /** 领取后生效天数。 */
    private Integer fixedStartTerm;
    /** 生效后有效天数。 */
    private Integer fixedEndTerm;
    /** 已核销数量。 */
    private Integer useCount;
    @TableLogic(value = "0", delval = "1")
    private Boolean deleted;
}
