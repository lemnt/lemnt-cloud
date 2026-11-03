package com.lemnt.order.infrastructure.payment.gateway;
import com.lemnt.order.domain.aftersale.model.AfterSale;
import com.lemnt.order.domain.payment.gateway.PaymentGateway;
import com.lemnt.order.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.client.PaymentClient;
import com.lemnt.pay.client.bo.PaymentRefundBo;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
/** 超时沿用原afterSaleNo查询或重试，不产生第二笔退款。 */
@Component
public class PaymentGatewayImpl implements PaymentGateway {
    @DubboReference(check=false,timeout=5000,retries=0) private PaymentClient client;
    public PaymentRefund refund(AfterSale af){
        if(af.amount()==null || af.amount().signum()<=0)throw new IllegalArgumentException("零现金售后不能发起资金请求");
        var v=client.refund(new PaymentRefundBo(af.refundNo(),af.orderNo(),af.paymentNo(),af.memberId(),af.amount(),af.refundRuleVersion()));
        return new PaymentRefund(v.refundNo(),v.afterSaleNo(),v.paymentNo(),v.orderNo(),v.channel(),v.amount(),v.status(),v.channelRefundNo());
    }
}
