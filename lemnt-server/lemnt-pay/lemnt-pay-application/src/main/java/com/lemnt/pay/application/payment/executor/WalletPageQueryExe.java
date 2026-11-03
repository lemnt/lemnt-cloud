package com.lemnt.pay.application.payment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.pay.application.payment.converter.WalletDTOConverter;
import com.lemnt.pay.application.payment.dto.WalletDTO;
import com.lemnt.pay.application.payment.qry.WalletPageQry;
import com.lemnt.pay.application.payment.support.PayConfigRules;
import com.lemnt.pay.domain.payment.gateway.WalletGateway;
import com.lemnt.pay.domain.payment.model.WalletFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 后台钱包分页查询。 */
@Component
@RequiredArgsConstructor
public class WalletPageQueryExe {
    private final WalletGateway wallets;

    public PageResult<WalletDTO> execute(WalletPageQry qry) {
        if (qry == null) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        PayConfigRules.page(qry.pageNum(), qry.pageSize());
        var page = wallets.page(new WalletFilter(qry.memberId(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(
                page.getRecords().stream().map(WalletDTOConverter::wallet).toList(),
                page.getTotal());
    }
}
