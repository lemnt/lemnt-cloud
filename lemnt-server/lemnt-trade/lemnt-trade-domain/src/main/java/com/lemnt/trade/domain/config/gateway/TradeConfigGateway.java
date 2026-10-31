package com.lemnt.trade.domain.config.gateway;

import com.lemnt.trade.domain.config.model.TradeConfig;

/** trade_config 领域存储接口。 */
public interface TradeConfigGateway {
    /** 读取配置。 */
    TradeConfig get();

    /** 锁定记录。 */
    void lock();

    /** 修改记录。 */
    void update(TradeConfig value);

}
