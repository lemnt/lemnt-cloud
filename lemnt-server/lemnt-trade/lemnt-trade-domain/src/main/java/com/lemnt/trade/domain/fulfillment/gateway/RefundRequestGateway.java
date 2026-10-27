package com.lemnt.trade.domain.fulfillment.gateway;

import com.lemnt.trade.domain.fulfillment.model.FulfillmentFilter;
import com.lemnt.trade.domain.fulfillment.model.RefundPage;
import com.lemnt.trade.domain.fulfillment.model.RefundRequest;

import java.time.LocalDateTime;

/** RefundRequest 表领域存储接口。 */
public interface RefundRequestGateway {
    /**
     * 有效退款。
     */
    boolean activeRefund(String orderNo);

    /**
     * 查询退款。
     */
    RefundPage refunds(FulfillmentFilter filter);

    /**
     * 退款。
     */
    RefundRequest refund(String refundNo);

    /**
     * 锁定退款。
     */
    RefundRequest lockRefund(String refundNo);

    /**
     * 查找退款请求。
     */
    RefundRequest findRefundRequest(String orderNo, String requestNo);

    /**
     * 保存退款请求。
     */
    void saveRefundRequest(RefundRequest request);

    /**
     * 拒绝退款。
     */
    boolean rejectRefund(String refundNo, String reason, LocalDateTime time);
}
