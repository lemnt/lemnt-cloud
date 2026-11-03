package com.lemnt.order.infrastructure.brokerage.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.order.domain.brokerage.gateway.BrokerageWithdrawGateway;
import com.lemnt.order.domain.brokerage.model.BrokerageWithdraw;
import com.lemnt.order.domain.brokerage.model.BrokerageWithdrawFilter;
import com.lemnt.order.infrastructure.brokerage.converter.BrokerageConverter;
import com.lemnt.order.infrastructure.brokerage.dataobject.BrokerageWithdrawDO;
import com.lemnt.order.infrastructure.brokerage.mapper.BrokerageWithdrawMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/** 提现单仅操作对应表，审核使用 CAS。 */
@Component
@RequiredArgsConstructor
public class BrokerageWithdrawGatewayImpl implements BrokerageWithdrawGateway {
    private final BrokerageWithdrawMapper mapper;

    @Override
    public BrokerageWithdraw find(Long id) {
        return BrokerageConverter.withdraw(mapper.selectById(id));
    }

    @Override
    public BrokerageWithdraw lock(Long id) {
        return BrokerageConverter.withdraw(
                mapper.selectOne(
                        Wrappers.<BrokerageWithdrawDO>lambdaQuery()
                                .eq(BrokerageWithdrawDO::getId, id)
                                .last("FOR UPDATE")));
    }

    @Override
    public BrokerageWithdraw save(BrokerageWithdraw value) {
        var row = BrokerageConverter.row(value);
        mapper.insert(row);
        return BrokerageConverter.withdraw(row);
    }

    @Override
    public PageResult<BrokerageWithdraw> page(BrokerageWithdrawFilter filter) {
        var page =
                mapper.selectPage(
                        new Page<BrokerageWithdrawDO>(filter.pageNum(), filter.pageSize()),
                        Wrappers.<BrokerageWithdrawDO>lambdaQuery()
                                .eq(
                                        filter.userId() != null,
                                        BrokerageWithdrawDO::getUserId,
                                        filter.userId())
                                .eq(
                                        filter.status() != null,
                                        BrokerageWithdrawDO::getStatus,
                                        filter.status())
                                .orderByDesc(BrokerageWithdrawDO::getId));
        return PageResult.of(
                page.getRecords().stream().map(BrokerageConverter::withdraw).toList(),
                page.getTotal());
    }

    @Override
    public boolean updateStatus(
            Long id, Integer expected, Integer next, String auditReason, LocalDateTime auditTime) {
        return mapper.update(
                        null,
                        Wrappers.<BrokerageWithdrawDO>lambdaUpdate()
                                .eq(BrokerageWithdrawDO::getId, id)
                                .eq(BrokerageWithdrawDO::getStatus, expected)
                                .set(BrokerageWithdrawDO::getStatus, next)
                                .set(BrokerageWithdrawDO::getAuditReason, auditReason)
                                .set(BrokerageWithdrawDO::getAuditTime, auditTime))
                == 1;
    }
}
