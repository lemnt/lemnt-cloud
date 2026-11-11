package com.lemnt.order.application.brokerage.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.order.application.brokerage.dto.BrokerageRecordDTO;
import com.lemnt.order.application.brokerage.qry.BrokerageRecordPageQry;
import com.lemnt.order.application.brokerage.service.BrokerageService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 佣金记录分页执行器。
 */
@Component
@RequiredArgsConstructor
public class BrokerageRecordPageQueryExe {
    private final BrokerageService service;

    public PageResult<BrokerageRecordDTO> execute(BrokerageRecordPageQry qry) {
        return service.pageRecords(qry);
    }
}
