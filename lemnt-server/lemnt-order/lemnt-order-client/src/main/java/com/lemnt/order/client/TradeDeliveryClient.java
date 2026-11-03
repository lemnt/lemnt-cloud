package com.lemnt.order.client;

import com.lemnt.order.client.bo.DeliveryFeeBo;
import com.lemnt.order.client.dto.DeliveryFeeDTO;

/** 普通快递计费契约，只接受其他可信业务服务的商品快照。 */
public interface TradeDeliveryClient {
    /** 使用当前配置计算运费，不调用真实物流渠道。 */
    DeliveryFeeDTO price(DeliveryFeeBo input);
}
