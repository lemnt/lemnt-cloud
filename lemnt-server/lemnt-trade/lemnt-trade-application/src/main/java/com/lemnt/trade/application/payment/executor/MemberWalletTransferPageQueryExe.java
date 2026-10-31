package com.lemnt.trade.application.payment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.payment.converter.WalletTransferDTOConverter;
import com.lemnt.trade.application.payment.dto.WalletTransferDTO;
import com.lemnt.trade.application.payment.support.PayConfigRules;
import com.lemnt.trade.domain.payment.gateway.WalletTransferGateway;
import com.lemnt.trade.domain.payment.model.WalletTransferFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** App 本人转出或转入分页。 */
@Component
@RequiredArgsConstructor
public class MemberWalletTransferPageQueryExe {
    private final WalletTransferGateway transfers;

    public PageResult<WalletTransferDTO> execute(Long memberId, int pageNum, int pageSize) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("会员编号不合法");
        }
        PayConfigRules.page(pageNum, pageSize);
        var page = transfers.page(new WalletTransferFilter(memberId, null, null, null, pageNum, pageSize));
        return PageResult.of(page.getRecords().stream().map(WalletTransferDTOConverter::transfer).toList(),
                page.getTotal());
    }
}
