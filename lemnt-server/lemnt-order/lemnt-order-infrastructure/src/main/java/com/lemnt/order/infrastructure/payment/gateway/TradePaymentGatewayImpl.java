package com.lemnt.order.infrastructure.payment.gateway;
import com.lemnt.order.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.order.domain.payment.model.PaymentRecord;
import com.lemnt.pay.client.PaymentQueryClient;
import com.lemnt.pay.client.dto.PaymentResultDTO;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;
/** 原支付编号是跨服务身份；没有资金库主键时保留null，不补造ID。 */
@Component
public class TradePaymentGatewayImpl implements TradePaymentGateway {
    @DubboReference(check=false,timeout=5000,retries=0) private PaymentQueryClient client;
    public List<PaymentRecord> findByOrderNo(String no){return client.findByOrderNo(no).stream().map(this::from).toList();}
    public PaymentRecord findByPaymentNo(String no){return from(client.find(no));}
    private PaymentRecord from(PaymentResultDTO v){return v==null?null:new PaymentRecord(null,v.paymentNo(),v.orderNo(),v.channel(),v.appKey(),v.channelTradeNo(),v.amount(),v.status(),null);}
}
