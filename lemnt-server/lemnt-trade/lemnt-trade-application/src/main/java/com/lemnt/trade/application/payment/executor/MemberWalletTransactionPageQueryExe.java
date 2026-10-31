package com.lemnt.trade.application.payment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.payment.converter.WalletDTOConverter;
import com.lemnt.trade.application.payment.dto.WalletTransactionDTO;
import com.lemnt.trade.application.payment.qry.WalletTransactionPageQry;
import com.lemnt.trade.domain.payment.gateway.WalletGateway;
import com.lemnt.trade.domain.payment.model.WalletBizType;
import com.lemnt.trade.domain.payment.model.WalletTransactionFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** App 本人钱包流水分页，先确保钱包存在。 */
@Component
@RequiredArgsConstructor
public class MemberWalletTransactionPageQueryExe {
    private final WalletGateway wallets;

    public PageResult<WalletTransactionDTO> execute(Long memberId, WalletTransactionPageQry qry) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("会员编号不合法");
        }
        if (qry == null || qry.pageNum() < 1 || qry.pageNum() > 1000000
                || qry.pageSize() < 1 || qry.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        String bizType = qry.bizType() == null || qry.bizType().isBlank() ? null : qry.bizType();
        if (bizType != null) {
            try {
                WalletBizType.valueOf(bizType);
            } catch (IllegalArgumentException error) {
                throw new IllegalArgumentException("业务类型不合法");
            }
        }
        var wallet = wallets.createIfAbsent(memberId);
        var page = wallets.transactionPage(new WalletTransactionFilter(wallet.id(), bizType,
                qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(WalletDTOConverter::transaction).toList(),
                page.getTotal());
    }
}
