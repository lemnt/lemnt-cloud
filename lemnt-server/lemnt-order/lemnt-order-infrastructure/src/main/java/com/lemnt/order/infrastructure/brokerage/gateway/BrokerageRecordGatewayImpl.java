package com.lemnt.order.infrastructure.brokerage.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.order.domain.brokerage.gateway.BrokerageRecordGateway;
import com.lemnt.order.domain.brokerage.model.BrokerageRecord;
import com.lemnt.order.domain.brokerage.model.BrokerageRecordFilter;
import com.lemnt.order.infrastructure.brokerage.converter.BrokerageConverter;
import com.lemnt.order.infrastructure.brokerage.dataobject.BrokerageRecordDO;
import com.lemnt.order.infrastructure.brokerage.mapper.BrokerageRecordMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 佣金记录仅操作对应表，状态变更使用 CAS。
 */
@Component
@RequiredArgsConstructor
public class BrokerageRecordGatewayImpl implements BrokerageRecordGateway {
    private final BrokerageRecordMapper mapper;

    @Override
    public BrokerageRecord find(Long id) {
        return BrokerageConverter.record(mapper.selectById(id));
    }

    @Override
    public BrokerageRecord findByBiz(Long userId, Integer bizType, String bizId) {
        return BrokerageConverter.record(
            mapper.selectOne(
                Wrappers.<BrokerageRecordDO>lambdaQuery()
                    .eq(BrokerageRecordDO::getUserId, userId)
                    .eq(BrokerageRecordDO::getBizType, bizType)
                    .eq(BrokerageRecordDO::getBizId, bizId)
                    .last("LIMIT 1")));
    }

    @Override
    public BrokerageRecord save(BrokerageRecord value) {
        var row = BrokerageConverter.row(value);
        mapper.insert(row);
        return BrokerageConverter.record(row);
    }

    @Override
    public PageResult<BrokerageRecord> page(BrokerageRecordFilter filter) {
        var page =
            mapper.selectPage(
                new Page<BrokerageRecordDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<BrokerageRecordDO>lambdaQuery()
                    .eq(
                        filter.userId() != null,
                        BrokerageRecordDO::getUserId,
                        filter.userId())
                    .eq(
                        filter.bizType() != null,
                        BrokerageRecordDO::getBizType,
                        filter.bizType())
                    .eq(
                        filter.status() != null,
                        BrokerageRecordDO::getStatus,
                        filter.status())
                    .orderByDesc(BrokerageRecordDO::getId));
        return PageResult.of(
            page.getRecords().stream().map(BrokerageConverter::record).toList(),
            page.getTotal());
    }

    @Override
    public List<BrokerageRecord> byBiz(Integer bizType, String bizId) {
        return mapper
            .selectList(
                Wrappers.<BrokerageRecordDO>lambdaQuery()
                    .eq(BrokerageRecordDO::getBizType, bizType)
                    .eq(BrokerageRecordDO::getBizId, bizId))
            .stream()
            .map(BrokerageConverter::record)
            .toList();
    }

    @Override
    public List<BrokerageRecord> due(Integer status, LocalDateTime now, int limit) {
        return mapper
            .selectList(
                Wrappers.<BrokerageRecordDO>lambdaQuery()
                    .eq(BrokerageRecordDO::getStatus, status)
                    .le(BrokerageRecordDO::getUnfreezeTime, now)
                    .orderByAsc(BrokerageRecordDO::getUnfreezeTime)
                    .last("LIMIT " + limit))
            .stream()
            .map(BrokerageConverter::record)
            .toList();
    }

    @Override
    public boolean updateStatus(Long id, Integer expected, Integer next) {
        return mapper.update(
            null,
            Wrappers.<BrokerageRecordDO>lambdaUpdate()
                .eq(BrokerageRecordDO::getId, id)
                .eq(BrokerageRecordDO::getStatus, expected)
                .set(BrokerageRecordDO::getStatus, next))
            == 1;
    }

    @Override
    public boolean updatePrice(Long id, BigDecimal expected, BigDecimal next) {
        return mapper.update(
            null,
            Wrappers.<BrokerageRecordDO>lambdaUpdate()
                .eq(BrokerageRecordDO::getId, id)
                .eq(BrokerageRecordDO::getPrice, expected)
                .set(BrokerageRecordDO::getPrice, next))
            == 1;
    }
}
