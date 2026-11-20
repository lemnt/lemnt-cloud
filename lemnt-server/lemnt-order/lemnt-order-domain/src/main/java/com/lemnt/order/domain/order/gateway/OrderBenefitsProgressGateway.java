package com.lemnt.order.domain.order.gateway;

import com.lemnt.order.domain.aftersale.model.AfterSale;

/**
 * 只根据所属服务的真实消费凭证和库存记录判断处理完成。
 */
public interface OrderBenefitsProgressGateway {
    boolean paid(String orderNo);

    boolean refunded(AfterSale afterSale);
}
