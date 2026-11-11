package com.lemnt.order.application.brokerage.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.order.application.brokerage.dto.BrokerageUserDTO;
import com.lemnt.order.application.brokerage.qry.BrokerageUserPageQry;
import com.lemnt.order.application.brokerage.service.BrokerageService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 分销用户分页执行器。
 */
@Component
@RequiredArgsConstructor
public class BrokerageUserPageQueryExe {
    private final BrokerageService service;

    public PageResult<BrokerageUserDTO> execute(BrokerageUserPageQry qry) {
        return service.pageUsers(qry);
    }
}
