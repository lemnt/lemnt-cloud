package com.lemnt.pay.client.bo;

import java.io.Serializable;

/** 可信业务服务的预支付入参，订单金额由服务端原单读取。 */
public record PaymentPrepayBo(Long memberId, String orderNo, String appKey, String channel)
        implements Serializable {}
