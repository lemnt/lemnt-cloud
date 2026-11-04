package com.lemnt.order.application.brokerage.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.order.application.brokerage.dto.BrokerageWithdrawDTO;
import com.lemnt.order.application.brokerage.qry.BrokerageWithdrawPageQry;
import com.lemnt.order.application.brokerage.service.BrokerageWithdrawService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 提现单分页执行器。
 */
@Component
@RequiredArgsConstructor
public class BrokerageWithdrawPageQueryExe {
    private final BrokerageWithdrawService service;

    public PageResult<BrokerageWithdrawDTO> execute(BrokerageWithdrawPageQry qry) {
        return service.page(qry);
    }
}
