package com.lemnt.trade.application.payment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.payment.converter.PayConfigDTOConverter;
import com.lemnt.trade.application.payment.dto.PayChannelConfigDTO;
import com.lemnt.trade.application.payment.qry.PayChannelPageQry;
import com.lemnt.trade.application.payment.support.PayConfigRules;
import com.lemnt.trade.domain.payment.gateway.PayChannelConfigGateway;
import com.lemnt.trade.domain.payment.model.PayChannelFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 支付渠道配置分页查询。 */
@Component
@RequiredArgsConstructor
public class PayChannelPageQueryExe {
    private final PayChannelConfigGateway channels;

    public PageResult<PayChannelConfigDTO> execute(PayChannelPageQry qry) {
        if (qry == null) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        PayConfigRules.page(qry.pageNum(), qry.pageSize());
        var page = channels.page(new PayChannelFilter(qry.appId(),
                PayConfigRules.optional(qry.code(), 32, "渠道编码"), qry.status(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(PayConfigDTOConverter::payChannel).toList(),
                page.getTotal());
    }
}
