package com.lemnt.trade.infrastructure.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.payment.gateway.PayAppGateway;
import com.lemnt.trade.domain.payment.model.PayApp;
import com.lemnt.trade.domain.payment.model.PayAppFilter;
import com.lemnt.trade.infrastructure.payment.converter.PayAppConverter;
import com.lemnt.trade.infrastructure.payment.dataobject.PayAppDO;
import com.lemnt.trade.infrastructure.payment.mapper.PayAppMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** trade_pay_app 持久化实现。 */
@Component
@RequiredArgsConstructor
public class PayAppGatewayImpl implements PayAppGateway {
    private final PayAppMapper mapper;

    @Override
    public PageResult<PayApp> page(PayAppFilter filter) {
        var page = mapper.selectPage(new Page<PayAppDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<PayAppDO>lambdaQuery()
                        .like(filter.appKey() != null && !filter.appKey().isBlank(), PayAppDO::getAppKey, filter.appKey())
                        .like(filter.name() != null && !filter.name().isBlank(), PayAppDO::getName, filter.name())
                        .eq(filter.status() != null, PayAppDO::getStatus, filter.status())
                        .orderByDesc(PayAppDO::getId));
        return PageResult.of(page.getRecords().stream().map(PayAppConverter::toEntity).toList(), page.getTotal());
    }

    @Override
    public PayApp find(Long id) {
        return PayAppConverter.toEntity(mapper.selectById(id));
    }

    @Override
    public PayApp findByAppKey(String appKey) {
        return PayAppConverter.toEntity(mapper.selectOne(Wrappers.<PayAppDO>lambdaQuery()
                .eq(PayAppDO::getAppKey, appKey).last("LIMIT 1")));
    }

    @Override
    public boolean existsAppKey(String appKey, Long excludeId) {
        return mapper.selectCount(Wrappers.<PayAppDO>lambdaQuery()
                .eq(PayAppDO::getAppKey, appKey)
                .ne(excludeId != null, PayAppDO::getId, excludeId)) > 0;
    }

    @Override
    public PayApp insert(PayApp value) {
        var row = PayAppConverter.toDO(value);
        mapper.insert(row);
        return PayAppConverter.toEntity(row);
    }

    @Override
    public boolean update(PayApp value) {
        return mapper.updateById(PayAppConverter.toDO(value)) == 1;
    }

    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) == 1;
    }
}
