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

/**
 * 结算数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_checkout")
public class CheckoutDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String orderNo;
    private Long memberId;
    private String requestNo;
    private String fingerprint;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;
    private Long couponId;
}
