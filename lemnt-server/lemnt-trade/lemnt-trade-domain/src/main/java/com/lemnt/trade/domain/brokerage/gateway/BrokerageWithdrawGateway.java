package com.lemnt.trade.domain.brokerage.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.brokerage.model.BrokerageWithdraw;
import com.lemnt.trade.domain.brokerage.model.BrokerageWithdrawFilter;

/** 提现单存储契约。 */
public interface BrokerageWithdrawGateway {
    BrokerageWithdraw find(Long id);

    /** 锁定提现单行，调用方持有事务。 */
    BrokerageWithdraw lock(Long id);

    BrokerageWithdraw save(BrokerageWithdraw value);

    PageResult<BrokerageWithdraw> page(BrokerageWithdrawFilter filter);

    boolean updateStatus(
            Long id,
            Integer expected,
            Integer next,
            String auditReason,
            java.time.LocalDateTime auditTime);
}
