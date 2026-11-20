package com.lemnt.order.domain.config.gateway;

import com.lemnt.order.domain.config.model.TradeConfig;

/**
 * 交易配置组领域存储接口。
 */
public interface TradeConfigGateway {
    /**
     * 读取配置。
     */
    TradeConfig get();

    /**
     * 修改记录。
     */
    void update(TradeConfig value);
}
