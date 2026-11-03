package com.lemnt.order.infrastructure.paymentrefund.gateway;
import com.lemnt.order.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.order.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.order.domain.paymentrefund.model.PaymentRefundTotal;
import com.lemnt.pay.client.PaymentQueryClient;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
/** 统计及明细都由资金所属服务提供，不在订单库JOIN支付表。 */
@Component
public class PaymentRefundGatewayImpl implements PaymentRefundGateway {
    @DubboReference(check=false,timeout=5000,retries=0) private PaymentQueryClient client;
    public PaymentRefundTotal total(String no){var v=client.refundTotal(no);return new PaymentRefundTotal(v.refundCount(),v.refundAmount());}
    public PaymentRefund findByAfterSaleNo(String no){var v=client.refundByAfterSale(no);return v==null?null:new PaymentRefund(v.refundNo(),v.afterSaleNo(),v.paymentNo(),v.orderNo(),v.channel(),v.amount(),v.status(),v.channelRefundNo());}
}
