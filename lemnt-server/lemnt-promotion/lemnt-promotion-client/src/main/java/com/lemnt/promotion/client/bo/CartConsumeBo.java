package com.lemnt.promotion.client.bo;

import java.io.Serializable;
import java.util.List;

/** 按订单及购物车原版本消费，避免重复删除新版本行。 */
public record CartConsumeBo(String orderNo, Long memberId, List<Reference> references)
        implements Serializable {
    public record Reference(Long cartId, long version) implements Serializable {}
}
