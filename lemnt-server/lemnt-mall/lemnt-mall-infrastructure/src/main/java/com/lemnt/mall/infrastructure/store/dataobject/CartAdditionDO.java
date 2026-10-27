package com.lemnt.mall.infrastructure.store.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 购物车增量加购数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_cart_add_request")
public class CartAdditionDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private String requestNo;
    private Long productId;
    private int quantity;
    private Long cartId;
    private int resultQuantity;
    private boolean selected;
    private long cartVersion;
}
