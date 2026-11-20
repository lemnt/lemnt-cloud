package com.lemnt.pay.infrastructure.channel.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.pay.domain.channel.gateway.PayChannelConfigGateway;
import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.channel.model.PayChannelFilter;
import com.lemnt.pay.infrastructure.channel.converter.PayChannelConfigConverter;
import com.lemnt.pay.infrastructure.channel.dataobject.PayChannelConfigDO;
import com.lemnt.pay.infrastructure.channel.mapper.PayChannelConfigMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * pay_channel 持久化实现。
 */
@Component
@RequiredArgsConstructor
public class PayChannelConfigGatewayImpl implements PayChannelConfigGateway {
    private final PayChannelConfigMapper mapper;

    @Override
    public PageResult<PayChannelConfig> page(PayChannelFilter filter) {
        var page =
            mapper.selectPage(
                new Page<PayChannelConfigDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<PayChannelConfigDO>lambdaQuery()
                    .eq(
                        filter.appId() != null,
                        PayChannelConfigDO::getAppId,
                        filter.appId())
                    .like(
                        filter.code() != null && !filter.code().isBlank(),
                        PayChannelConfigDO::getCode,
                        filter.code())
                    .eq(
                        filter.status() != null,
                        PayChannelConfigDO::getStatus,
                        filter.status())
                    .orderByDesc(PayChannelConfigDO::getId));
        return PageResult.of(
            page.getRecords().stream().map(PayChannelConfigConverter::toEntity).toList(),
            page.getTotal());
    }

    @Override
    public PayChannelConfig find(Long id) {
        return PayChannelConfigConverter.toEntity(mapper.selectById(id));
    }

    @Override
    public List<PayChannelConfig> listByAppId(Long appId) {
        return mapper
            .selectList(
                Wrappers.<PayChannelConfigDO>lambdaQuery()
                    .eq(PayChannelConfigDO::getAppId, appId)
                    .orderByAsc(PayChannelConfigDO::getCode))
            .stream()
            .map(PayChannelConfigConverter::toEntity)
            .toList();
    }

    @Override
    public boolean exists(Long appId, String code, Long excludeId) {
        return mapper.selectCount(
            Wrappers.<PayChannelConfigDO>lambdaQuery()
                .eq(PayChannelConfigDO::getAppId, appId)
                .eq(PayChannelConfigDO::getCode, code)
                .ne(excludeId != null, PayChannelConfigDO::getId, excludeId))
            > 0;
    }

    @Override
    public PayChannelConfig insert(PayChannelConfig value) {
        var row = PayChannelConfigConverter.toDO(value);
        mapper.insert(row);
        return PayChannelConfigConverter.toEntity(row);
    }

    @Override
    public boolean update(PayChannelConfig value) {
        return mapper.updateById(PayChannelConfigConverter.toDO(value)) == 1;
    }

    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) == 1;
    }

    @Override
    public boolean channelReferenced(Long appId) {
        return mapper.selectCount(
            Wrappers.<PayChannelConfigDO>lambdaQuery()
                .eq(PayChannelConfigDO::getAppId, appId))
            > 0;
    }
}
