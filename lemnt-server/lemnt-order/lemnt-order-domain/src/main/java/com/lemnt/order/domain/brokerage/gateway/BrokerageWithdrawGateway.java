package com.lemnt.order.domain.brokerage.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.order.domain.brokerage.model.BrokerageWithdraw;
import com.lemnt.order.domain.brokerage.model.BrokerageWithdrawFilter;

import java.time.LocalDateTime;

/** 提现单存储契约。 */
public interface BrokerageWithdrawGateway {
    BrokerageWithdraw find(Long id);

    /** 锁定提现单行，调用方持有事务。 */
    BrokerageWithdraw lock(Long id);

    BrokerageWithdraw save(BrokerageWithdraw value);

    PageResult<BrokerageWithdraw> page(BrokerageWithdrawFilter filter);

    boolean updateStatus(
            Long id, Integer expected, Integer next, String auditReason, LocalDateTime auditTime);
}
