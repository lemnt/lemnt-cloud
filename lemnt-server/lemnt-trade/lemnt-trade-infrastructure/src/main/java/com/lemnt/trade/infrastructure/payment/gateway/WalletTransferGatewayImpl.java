package com.lemnt.trade.infrastructure.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.payment.gateway.WalletTransferGateway;
import com.lemnt.trade.domain.payment.model.WalletTransfer;
import com.lemnt.trade.domain.payment.model.WalletTransferFilter;
import com.lemnt.trade.infrastructure.payment.converter.WalletTransferConverter;
import com.lemnt.trade.infrastructure.payment.dataobject.WalletTransferDO;
import com.lemnt.trade.infrastructure.payment.mapper.WalletTransferMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** trade_member_wallet_transfer 持久化实现。 */
@Component
@RequiredArgsConstructor
public class WalletTransferGatewayImpl implements WalletTransferGateway {
    private final WalletTransferMapper mapper;

    @Override
    public WalletTransfer createIfAbsent(WalletTransfer value) {
        try {
            var row = WalletTransferConverter.toDO(value);
            mapper.insert(row);
            return WalletTransferConverter.toEntity(row);
        } catch (DuplicateKeyException duplicate) {
            return findByRequestNo(value.requestNo());
        }
    }

    @Override
    public WalletTransfer findByNo(String transferNo) {
        return WalletTransferConverter.toEntity(mapper.selectOne(Wrappers.<WalletTransferDO>lambdaQuery()
                .eq(WalletTransferDO::getTransferNo, transferNo).last("LIMIT 1")));
    }

    @Override
    public WalletTransfer findByRequestNo(String requestNo) {
        return WalletTransferConverter.toEntity(mapper.selectOne(Wrappers.<WalletTransferDO>lambdaQuery()
                .eq(WalletTransferDO::getRequestNo, requestNo).last("LIMIT 1")));
    }

    @Override
    public PageResult<WalletTransfer> page(WalletTransferFilter filter) {
        var page = mapper.selectPage(new Page<WalletTransferDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<WalletTransferDO>lambdaQuery()
                        .and(filter.memberId() != null, query -> query
                                .eq(WalletTransferDO::getFromMemberId, filter.memberId())
                                .or().eq(WalletTransferDO::getToMemberId, filter.memberId()))
                        .eq(filter.fromMemberId() != null, WalletTransferDO::getFromMemberId, filter.fromMemberId())
                        .eq(filter.toMemberId() != null, WalletTransferDO::getToMemberId, filter.toMemberId())
                        .eq(filter.status() != null, WalletTransferDO::getStatus, filter.status())
                        .orderByDesc(WalletTransferDO::getId));
        return PageResult.of(page.getRecords().stream().map(WalletTransferConverter::toEntity).toList(),
                page.getTotal());
    }
}
