package com.lemnt.trade.infrastructure.fulfillment.gateway;

import com.lemnt.trade.domain.fulfillment.gateway.RefundRequestGateway;
import com.lemnt.trade.domain.fulfillment.model.FulfillmentFilter;
import com.lemnt.trade.domain.fulfillment.model.RefundPage;
import com.lemnt.trade.domain.fulfillment.model.RefundRequest;
import com.lemnt.trade.infrastructure.fulfillment.converter.FulfillmentConverter;
import com.lemnt.trade.infrastructure.fulfillment.mapper.RefundRequestMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/** RefundRequest 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class RefundRequestGatewayImpl implements RefundRequestGateway {
    private final RefundRequestMapper refundRequestMapper;

    @Override
    /**
     * 有效退款。
     */
    public boolean activeRefund(String no) {
        return refundRequestMapper.activeRefund(no) > 0;
    }

    @Override
    /**
     * 查询退款。
     */
    @Transactional(readOnly = true)
    public RefundPage refunds(FulfillmentFilter filter) {
        return new RefundPage(
                refundRequestMapper.refunds(filter).stream().map(FulfillmentConverter::refundRequest).toList(),
                refundRequestMapper.refundCount(filter));
    }

    @Override
    /**
     * 退款。
     */
    public RefundRequest refund(String no) {
        return FulfillmentConverter.refundRequest(refundRequestMapper.refund(no));
    }

    @Override
    /**
     * 锁定退款。
     */
    public RefundRequest lockRefund(String no) {
        return FulfillmentConverter.refundRequest(refundRequestMapper.lockRefund(no));
    }

    @Override
    /**
     * 查找退款请求。
     */
    public RefundRequest findRefundRequest(String orderNo, String key) {
        return FulfillmentConverter.refundRequest(refundRequestMapper.findRequest(orderNo, key));
    }

    @Override
    /**
     * 保存退款请求。
     */
    public void saveRefundRequest(RefundRequest value) {
        refundRequestMapper.insertRefund(FulfillmentConverter.refundRequest(value));
    }

    @Override
    /**
     * 拒绝退款。
     */
    public boolean rejectRefund(String no, String reason, LocalDateTime time) {
        return refundRequestMapper.reject(no, reason, time) == 1;
    }
}
