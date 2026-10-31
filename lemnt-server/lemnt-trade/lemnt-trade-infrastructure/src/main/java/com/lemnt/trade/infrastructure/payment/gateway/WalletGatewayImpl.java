package com.lemnt.trade.infrastructure.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.payment.gateway.WalletGateway;
import com.lemnt.trade.domain.payment.model.Wallet;
import com.lemnt.trade.domain.payment.model.WalletFilter;
import com.lemnt.trade.domain.payment.model.WalletTransaction;
import com.lemnt.trade.domain.payment.model.WalletTransactionFilter;
import com.lemnt.trade.infrastructure.payment.converter.WalletConverter;
import com.lemnt.trade.infrastructure.payment.dataobject.WalletDO;
import com.lemnt.trade.infrastructure.payment.dataobject.WalletTransactionDO;
import com.lemnt.trade.infrastructure.payment.mapper.WalletMapper;
import com.lemnt.trade.infrastructure.payment.mapper.WalletTransactionMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/** trade_member_wallet/transaction 持久化实现。 */
@Component
@RequiredArgsConstructor
public class WalletGatewayImpl implements WalletGateway {
    private final WalletMapper wallets;
    private final WalletTransactionMapper transactions;

    @Override
    public Wallet findByMemberId(Long memberId) {
        return WalletConverter.toEntity(wallets.selectOne(Wrappers.<WalletDO>lambdaQuery()
                .eq(WalletDO::getMemberId, memberId).last("LIMIT 1")));
    }

    @Override
    public Wallet lockByMemberId(Long memberId) {
        return WalletConverter.toEntity(wallets.lockByMemberId(memberId));
    }

    @Override
    public Wallet lockById(Long id) {
        return WalletConverter.toEntity(wallets.lockById(id));
    }

    @Override
    public Wallet createIfAbsent(Long memberId) {
        var existing = findByMemberId(memberId);
        if (existing != null) {
            return existing;
        }
        try {
            var row = new WalletDO();
            row.setMemberId(memberId);
            row.setBalance(BigDecimal.ZERO.setScale(2));
            row.setFreezePrice(BigDecimal.ZERO.setScale(2));
            row.setTotalExpense(BigDecimal.ZERO.setScale(2));
            row.setTotalRecharge(BigDecimal.ZERO.setScale(2));
            wallets.insert(row);
            return WalletConverter.toEntity(row);
        } catch (DuplicateKeyException duplicate) {
            return findByMemberId(memberId);
        }
    }

    @Override
    public boolean update(Wallet wallet) {
        return wallets.updateById(WalletConverter.toDO(wallet)) == 1;
    }

    @Override
    public PageResult<Wallet> page(WalletFilter filter) {
        var page = wallets.selectPage(new Page<WalletDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<WalletDO>lambdaQuery()
                        .eq(filter.memberId() != null, WalletDO::getMemberId, filter.memberId())
                        .orderByDesc(WalletDO::getId));
        return PageResult.of(page.getRecords().stream().map(WalletConverter::toEntity).toList(), page.getTotal());
    }

    @Override
    public WalletTransaction findTransaction(Long walletId, String bizType, String bizNo) {
        return WalletConverter.toEntity(transactions.selectOne(Wrappers.<WalletTransactionDO>lambdaQuery()
                .eq(WalletTransactionDO::getWalletId, walletId)
                .eq(WalletTransactionDO::getBizType, bizType)
                .eq(WalletTransactionDO::getBizNo, bizNo)
                .last("LIMIT 1")));
    }

    @Override
    public void insertTransaction(WalletTransaction transaction) {
        transactions.insert(WalletConverter.toDO(transaction));
    }

    @Override
    public PageResult<WalletTransaction> transactionPage(WalletTransactionFilter filter) {
        var page = transactions.selectPage(new Page<WalletTransactionDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<WalletTransactionDO>lambdaQuery()
                        .eq(WalletTransactionDO::getWalletId, filter.walletId())
                        .eq(filter.bizType() != null, WalletTransactionDO::getBizType, filter.bizType())
                        .orderByDesc(WalletTransactionDO::getId));
        return PageResult.of(page.getRecords().stream().map(WalletConverter::toEntity).toList(), page.getTotal());
    }
}
