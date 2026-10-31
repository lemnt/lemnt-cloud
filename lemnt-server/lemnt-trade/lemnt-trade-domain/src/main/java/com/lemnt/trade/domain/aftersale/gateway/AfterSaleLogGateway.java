package com.lemnt.trade.domain.aftersale.gateway;

import com.lemnt.trade.domain.aftersale.model.AfterSaleLog;

import java.util.List;

/** 售后日志表存储契约，不更新售后主体。 */
public interface AfterSaleLogGateway {
    /** 与售后状态变化同事务追加真实操作。 */
    void append(AfterSaleLog value);
    /** 按发生顺序读取售后日志。 */
    List<AfterSaleLog> byAfterSaleId(Long afterSaleId);
}
