package com.lemnt.mall.infrastructure.checkout.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 结算购物车数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_checkout_cart")
public class CheckoutCartDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String orderNo;
    private Long cartId;
    private Long cartVersion;
}
