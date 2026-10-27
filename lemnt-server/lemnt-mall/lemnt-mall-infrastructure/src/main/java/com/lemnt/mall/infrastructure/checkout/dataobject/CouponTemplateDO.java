package com.lemnt.mall.infrastructure.checkout.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_coupon_template")
public class CouponTemplateDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
}
