package com.lemnt.mall.domain.store.gateway;

import com.lemnt.mall.domain.store.model.CartAddition;

/** CartAddRequest 表领域存储接口。 */
public interface CartAddRequestGateway {
    /**
     * 增量加购。
     */
    CartAddition addition(Long memberId, String requestNo);

    /**
     * 增量加购。
     */
    void addition(CartAddition value);
}
