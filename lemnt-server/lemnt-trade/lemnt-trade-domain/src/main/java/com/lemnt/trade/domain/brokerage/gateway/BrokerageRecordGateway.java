package com.lemnt.trade.domain.brokerage.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.brokerage.model.BrokerageRecord;
import com.lemnt.trade.domain.brokerage.model.BrokerageRecordFilter;

import java.time.LocalDateTime;
import java.util.List;

/** 佣金记录存储契约。 */
public interface BrokerageRecordGateway {
    BrokerageRecord find(Long id);

    BrokerageRecord findByBiz(Long userId, Integer bizType, String bizId);

    BrokerageRecord save(BrokerageRecord value);

    PageResult<BrokerageRecord> page(BrokerageRecordFilter filter);

    List<BrokerageRecord> byBiz(Integer bizType, String bizId);

    List<BrokerageRecord> due(Integer status, LocalDateTime now, int limit);

    boolean updateStatus(Long id, Integer expected, Integer next);

    /** 按原金额 CAS 更新退款扣回累计金额，避免并发重复扣回。 */
    boolean updatePrice(Long id, java.math.BigDecimal expected, java.math.BigDecimal next);
}
