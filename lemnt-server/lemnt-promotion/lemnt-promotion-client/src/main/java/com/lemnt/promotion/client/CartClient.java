package com.lemnt.promotion.client;

import com.lemnt.promotion.client.bo.CartConsumeBo;
import com.lemnt.promotion.client.dto.CartSnapshotDTO;

import java.util.List;

/**
 * 可信购物车查询与按原版本消费，会员归属必须匹配。
 */
public interface CartClient {
    List<CartSnapshotDTO> cart(Long memberId);

    void consume(CartConsumeBo input);
}
