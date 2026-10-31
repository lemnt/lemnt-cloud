package com.lemnt.trade.application.payment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.payment.converter.RechargeDTOConverter;
import com.lemnt.trade.application.payment.dto.WalletRechargeDTO;
import com.lemnt.trade.application.payment.qry.WalletRechargePageQry;
import com.lemnt.trade.application.payment.support.PayConfigRules;
import com.lemnt.trade.application.payment.support.RechargeRules;
import com.lemnt.trade.domain.payment.gateway.WalletRechargeGateway;
import com.lemnt.trade.domain.payment.model.WalletRechargeFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 充值单分页查询。 */
@Component
@RequiredArgsConstructor
public class WalletRechargePageQueryExe {
    private final WalletRechargeGateway recharges;

    public PageResult<WalletRechargeDTO> execute(WalletRechargePageQry qry) {
        if (qry == null) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        PayConfigRules.page(qry.pageNum(), qry.pageSize());
        var page = recharges.page(new WalletRechargeFilter(qry.memberId(), RechargeRules.statusText(qry.status()),
                qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(RechargeDTOConverter::recharge).toList(),
                page.getTotal());
    }
}
