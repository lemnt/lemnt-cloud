package com.lemnt.trade.application.payment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.payment.converter.WalletTransferDTOConverter;
import com.lemnt.trade.application.payment.dto.WalletTransferDTO;
import com.lemnt.trade.application.payment.qry.WalletTransferPageQry;
import com.lemnt.trade.application.payment.support.PayConfigRules;
import com.lemnt.trade.domain.payment.gateway.WalletTransferGateway;
import com.lemnt.trade.domain.payment.model.WalletTransferFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 钱包转账分页查询。 */
@Component
@RequiredArgsConstructor
public class WalletTransferPageQueryExe {
    private final WalletTransferGateway transfers;

    public PageResult<WalletTransferDTO> execute(WalletTransferPageQry qry) {
        if (qry == null) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        PayConfigRules.page(qry.pageNum(), qry.pageSize());
        String status = PayConfigRules.optional(qry.status(), 20, "状态");
        if (status != null && !"SUCCESS".equals(status)) {
            throw new IllegalArgumentException("转账状态不合法");
        }
        var page = transfers.page(new WalletTransferFilter(qry.memberId(), qry.fromMemberId(), qry.toMemberId(),
                status, qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(WalletTransferDTOConverter::transfer).toList(),
                page.getTotal());
    }
}
