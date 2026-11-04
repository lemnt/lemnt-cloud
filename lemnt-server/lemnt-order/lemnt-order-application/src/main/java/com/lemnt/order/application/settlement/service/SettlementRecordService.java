package com.lemnt.order.application.settlement.service;

import com.lemnt.order.domain.cart.gateway.CartSnapshotGateway;
import com.lemnt.order.domain.settlement.gateway.SettlementGateway;
import com.lemnt.order.domain.settlement.model.SettlementOrder;
import com.lemnt.order.domain.settlement.model.SettlementRecord;
import com.lemnt.order.domain.settlement.model.SettlementSubmission;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 本域只保存结算恢复上下文，券与活动生命周期由营销服务处理。
 */
@Component
@RequiredArgsConstructor
public class SettlementRecordService {
    private final CartSnapshotGateway carts;
    private final SettlementGateway cache;

    public SettlementRecord findRequest(Long member, String request) {
        return cache.findRequest(member, request);
    }

    public SettlementRecord receipt(String no) {
        var value = cache.submission(no);
        return value == null ? null : value.record();
    }

    public void save(SettlementRecord record, SettlementOrder order) {
        cache.saveSubmission(new SettlementSubmission(order, record));
    }

    public void consumeCart(String no, Long member) {
        var record = receipt(no);
        if (record == null) throw new IllegalStateException("结算提交缓存已失效，请查询原订单");
        if (!member.equals(record.memberId())) throw new IllegalStateException("购物车引用与原会员不一致");
        carts.consume(no, member, record.cartReferences());
    }
}
