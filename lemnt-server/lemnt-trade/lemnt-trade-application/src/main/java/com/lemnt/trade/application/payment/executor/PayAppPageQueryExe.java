package com.lemnt.trade.application.payment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.payment.converter.PayConfigDTOConverter;
import com.lemnt.trade.application.payment.dto.PayAppDTO;
import com.lemnt.trade.application.payment.qry.PayAppPageQry;
import com.lemnt.trade.application.payment.support.PayConfigRules;
import com.lemnt.trade.domain.payment.gateway.PayAppGateway;
import com.lemnt.trade.domain.payment.model.PayAppFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 支付应用分页查询。 */
@Component
@RequiredArgsConstructor
public class PayAppPageQueryExe {
    private final PayAppGateway apps;

    public PageResult<PayAppDTO> execute(PayAppPageQry qry) {
        if (qry == null) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        PayConfigRules.page(qry.pageNum(), qry.pageSize());
        var page = apps.page(new PayAppFilter(PayConfigRules.optional(qry.appKey(), 64, "应用标识"),
                PayConfigRules.optional(qry.name(), 64, "应用名"), qry.status(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(PayConfigDTOConverter::payApp).toList(),
                page.getTotal());
    }
}
