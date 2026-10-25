package com.lemnt.trade.domain.checkout.gateway;

import com.lemnt.trade.domain.checkout.model.Settlement;

public interface SettlementGateway {
    void cache(Settlement value);

    Settlement cached(String id);

    String orderNo(Long memberId, String id);

    boolean abandoned(Long memberId, String id);

    void abandon(Long memberId, String id);

    void bind(Long memberId, String id, String orderNo);
}
