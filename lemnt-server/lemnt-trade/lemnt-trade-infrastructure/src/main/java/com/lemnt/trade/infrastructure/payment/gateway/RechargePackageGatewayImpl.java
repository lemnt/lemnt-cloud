package com.lemnt.trade.infrastructure.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.payment.gateway.RechargePackageGateway;
import com.lemnt.trade.domain.payment.model.RechargePackage;
import com.lemnt.trade.domain.payment.model.RechargePackageFilter;
import com.lemnt.trade.infrastructure.payment.converter.RechargeConverter;
import com.lemnt.trade.infrastructure.payment.dataobject.RechargePackageDO;
import com.lemnt.trade.infrastructure.payment.mapper.RechargePackageMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** trade_pay_recharge_package 持久化实现。 */
@Component
@RequiredArgsConstructor
public class RechargePackageGatewayImpl implements RechargePackageGateway {
    private final RechargePackageMapper mapper;

    @Override
    public PageResult<RechargePackage> page(RechargePackageFilter filter) {
        var page = mapper.selectPage(new Page<RechargePackageDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<RechargePackageDO>lambdaQuery()
                        .like(filter.name() != null && !filter.name().isBlank(), RechargePackageDO::getName, filter.name())
                        .eq(filter.status() != null, RechargePackageDO::getStatus, filter.status())
                        .orderByDesc(RechargePackageDO::getId));
        return PageResult.of(page.getRecords().stream().map(RechargeConverter::toEntity).toList(), page.getTotal());
    }

    @Override
    public RechargePackage find(Long id) {
        return RechargeConverter.toEntity(mapper.selectById(id));
    }

    @Override
    public List<RechargePackage> listEnabled() {
        return mapper.selectList(Wrappers.<RechargePackageDO>lambdaQuery()
                        .eq(RechargePackageDO::getStatus, 0)
                        .orderByAsc(RechargePackageDO::getPayPrice).orderByAsc(RechargePackageDO::getId))
                .stream().map(RechargeConverter::toEntity).toList();
    }

    @Override
    public boolean existsName(String name, Long excludeId) {
        return mapper.selectCount(Wrappers.<RechargePackageDO>lambdaQuery()
                .eq(RechargePackageDO::getName, name)
                .ne(excludeId != null, RechargePackageDO::getId, excludeId)) > 0;
    }

    @Override
    public RechargePackage insert(RechargePackage value) {
        var row = RechargeConverter.toDO(value);
        mapper.insert(row);
        return RechargeConverter.toEntity(row);
    }

    @Override
    public boolean update(RechargePackage value) {
        return mapper.updateById(RechargeConverter.toDO(value)) == 1;
    }

    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) == 1;
    }
}
