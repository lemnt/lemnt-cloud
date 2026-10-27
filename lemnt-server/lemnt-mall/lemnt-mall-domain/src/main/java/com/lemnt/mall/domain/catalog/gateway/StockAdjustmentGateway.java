package com.lemnt.mall.domain.catalog.gateway;

import com.lemnt.mall.domain.catalog.model.StockAdjustment;

/** StockAdjustment 表领域存储接口。 */
public interface StockAdjustmentGateway {
    /** 读取幂等库存调整记录。 */
    StockAdjustment find(String requestNo);

    /** 占用库存调整请求编号。 */
    void insert(StockAdjustment value);

    /** 记录调整后的库存。 */
    boolean finish(StockAdjustment value);
}
