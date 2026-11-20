package com.lemnt.pay.domain.payment.gateway;

import java.util.Map;

/** 支付应用业务通知的外部投递契约。 */
public interface PayAppNotifyGateway {
    /** 接收方确认处理后返回，其余结果由通知任务重试。 */
    void post(String url, Map<String, String> payload);
}
