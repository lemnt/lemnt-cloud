package com.lemnt.trade.application.payment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.payment.converter.WalletDTOConverter;
import com.lemnt.trade.application.payment.dto.WalletTransactionDTO;
import com.lemnt.trade.application.payment.qry.WalletTransactionPageQry;
import com.lemnt.trade.application.payment.support.PayConfigRules;
import com.lemnt.trade.domain.payment.gateway.WalletGateway;
import com.lemnt.trade.domain.payment.model.WalletBizType;
import com.lemnt.trade.domain.payment.model.WalletTransactionFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 会员钱包流水分页；无钱包时返回空页。 */
@Component
@RequiredArgsConstructor
public class WalletTransactionPageQueryExe {
    private final WalletGateway wallets;

    public PageResult<WalletTransactionDTO> execute(Long memberId, WalletTransactionPageQry qry) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("会员编号不合法");
        }
        if (qry == null) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        PayConfigRules.page(qry.pageNum(), qry.pageSize());
        String bizType = PayConfigRules.optional(qry.bizType(), 32, "业务类型");
        if (bizType != null) {
            try {
                WalletBizType.valueOf(bizType);
            } catch (IllegalArgumentException error) {
                throw new IllegalArgumentException("业务类型不合法");
            }
        }
        var wallet = wallets.findByMemberId(memberId);
        if (wallet == null) {
            return PageResult.of(List.of(), 0L);
        }
        var page = wallets.transactionPage(new WalletTransactionFilter(wallet.id(), bizType,
                qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(WalletDTOConverter::transaction).toList(),
                page.getTotal());
    }
}
