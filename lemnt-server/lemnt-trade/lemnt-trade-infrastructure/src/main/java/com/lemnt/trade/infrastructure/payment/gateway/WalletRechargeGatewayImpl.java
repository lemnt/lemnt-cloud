package com.lemnt.trade.infrastructure.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.payment.gateway.WalletRechargeGateway;
import com.lemnt.trade.domain.payment.model.WalletRecharge;
import com.lemnt.trade.domain.payment.model.WalletRechargeFilter;
import com.lemnt.trade.infrastructure.payment.converter.RechargeConverter;
import com.lemnt.trade.infrastructure.payment.dataobject.WalletRechargeDO;
import com.lemnt.trade.infrastructure.payment.mapper.WalletRechargeMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/** trade_pay_wallet_recharge 持久化实现。 */
@Component
@RequiredArgsConstructor
public class WalletRechargeGatewayImpl implements WalletRechargeGateway {
    private final WalletRechargeMapper mapper;

    @Override
    public WalletRecharge createIfAbsent(WalletRecharge value) {
        try {
            var row = RechargeConverter.toDO(value);
            mapper.insert(row);
            return RechargeConverter.toEntity(row);
        } catch (DuplicateKeyException duplicate) {
            return findByRequestNo(value.requestNo());
        }
    }

    @Override
    public WalletRecharge findByNo(String rechargeNo) {
        return RechargeConverter.toEntity(mapper.selectOne(Wrappers.<WalletRechargeDO>lambdaQuery()
                .eq(WalletRechargeDO::getRechargeNo, rechargeNo).last("LIMIT 1")));
    }

    @Override
    public WalletRecharge findByRequestNo(String requestNo) {
        return RechargeConverter.toEntity(mapper.selectOne(Wrappers.<WalletRechargeDO>lambdaQuery()
                .eq(WalletRechargeDO::getRequestNo, requestNo).last("LIMIT 1")));
    }

    @Override
    public WalletRecharge lockByNo(String rechargeNo) {
        return RechargeConverter.toEntity(mapper.lockByNo(rechargeNo));
    }

    @Override
    public boolean update(WalletRecharge value) {
        return mapper.updateById(RechargeConverter.toDO(value)) == 1;
    }

    @Override
    public PageResult<WalletRecharge> page(WalletRechargeFilter filter) {
        var page = mapper.selectPage(new Page<WalletRechargeDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<WalletRechargeDO>lambdaQuery()
                        .eq(filter.memberId() != null, WalletRechargeDO::getMemberId, filter.memberId())
                        .eq(filter.status() != null, WalletRechargeDO::getStatus, filter.status())
                        .orderByDesc(WalletRechargeDO::getId));
        return PageResult.of(page.getRecords().stream().map(RechargeConverter::toEntity).toList(), page.getTotal());
    }

    @Override
    public boolean packageReferenced(Long packageId) {
        return mapper.selectCount(Wrappers.<WalletRechargeDO>lambdaQuery()
                .eq(WalletRechargeDO::getPackageId, packageId)) > 0;
    }

    @Override
    public LocalDateTime databaseNow() {
        return mapper.databaseNow();
    }
}
