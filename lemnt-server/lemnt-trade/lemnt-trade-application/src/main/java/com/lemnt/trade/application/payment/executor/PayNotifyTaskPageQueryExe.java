package com.lemnt.trade.application.payment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.payment.converter.PayNotifyDTOConverter;
import com.lemnt.trade.application.payment.dto.PayNotifyTaskDTO;
import com.lemnt.trade.application.payment.qry.PayNotifyTaskPageQry;
import com.lemnt.trade.application.payment.support.PayConfigRules;
import com.lemnt.trade.domain.payment.gateway.PayNotifyTaskGateway;
import com.lemnt.trade.domain.payment.model.PayNotifyTaskFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 派发任务分页查询。 */
@Component
@RequiredArgsConstructor
public class PayNotifyTaskPageQueryExe {
    private final PayNotifyTaskGateway tasks;

    public PageResult<PayNotifyTaskDTO> execute(PayNotifyTaskPageQry qry) {
        if (qry == null) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        PayConfigRules.page(qry.pageNum(), qry.pageSize());
        var page = tasks.page(new PayNotifyTaskFilter(
                PayConfigRules.optional(qry.type(), 16, "类型"),
                PayConfigRules.optional(qry.bizNo(), 40, "业务单号"),
                PayConfigRules.optional(qry.orderNo(), 40, "订单编号"),
                PayConfigRules.optional(qry.status(), 20, "状态"),
                qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(PayNotifyDTOConverter::task).toList(),
                page.getTotal());
    }
}
