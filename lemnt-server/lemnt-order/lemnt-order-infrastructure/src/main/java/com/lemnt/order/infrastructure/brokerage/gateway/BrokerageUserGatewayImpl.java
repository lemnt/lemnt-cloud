package com.lemnt.order.infrastructure.brokerage.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.order.domain.brokerage.gateway.BrokerageUserGateway;
import com.lemnt.order.domain.brokerage.model.BrokerageUser;
import com.lemnt.order.domain.brokerage.model.BrokerageUserFilter;
import com.lemnt.order.infrastructure.brokerage.converter.BrokerageConverter;
import com.lemnt.order.infrastructure.brokerage.dataobject.BrokerageUserDO;
import com.lemnt.order.infrastructure.brokerage.mapper.BrokerageUserMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 分销用户仅操作对应表，锁定查询使用 FOR UPDATE。 */
@Component
@RequiredArgsConstructor
public class BrokerageUserGatewayImpl implements BrokerageUserGateway {
    private final BrokerageUserMapper mapper;

    @Override
    public BrokerageUser find(Long id) {
        return BrokerageConverter.user(mapper.selectById(id));
    }

    @Override
    public BrokerageUser lock(Long id) {
        return BrokerageConverter.user(mapper.lock(id));
    }

    @Override
    public BrokerageUser save(BrokerageUser value) {
        var row = BrokerageConverter.row(value);
        if (mapper.selectById(value.id()) == null) {
            mapper.insert(row);
        } else {
            mapper.updateById(row);
        }
        return BrokerageConverter.user(row);
    }

    @Override
    public boolean clearBind(Long id) {
        return mapper.update(
                        null,
                        Wrappers.<BrokerageUserDO>lambdaUpdate()
                                .eq(BrokerageUserDO::getId, id)
                                .set(BrokerageUserDO::getBindUserId, null)
                                .set(BrokerageUserDO::getBindUserTime, null))
                == 1;
    }

    @Override
    public PageResult<BrokerageUser> page(BrokerageUserFilter filter) {
        var page =
                mapper.selectPage(
                        new Page<BrokerageUserDO>(filter.pageNum(), filter.pageSize()),
                        Wrappers.<BrokerageUserDO>lambdaQuery()
                                .eq(
                                        filter.bindUserId() != null,
                                        BrokerageUserDO::getBindUserId,
                                        filter.bindUserId())
                                .eq(
                                        filter.brokerageEnabled() != null,
                                        BrokerageUserDO::getBrokerageEnabled,
                                        filter.brokerageEnabled())
                                .orderByDesc(BrokerageUserDO::getId));
        return PageResult.of(
                page.getRecords().stream().map(BrokerageConverter::user).toList(), page.getTotal());
    }

    @Override
    public long countChildren(Long bindUserId) {
        return mapper.selectCount(
                Wrappers.<BrokerageUserDO>lambdaQuery()
                        .eq(BrokerageUserDO::getBindUserId, bindUserId));
    }

    @Override
    public List<Long> childIds(Long bindUserId) {
        return mapper
                .selectList(
                        Wrappers.<BrokerageUserDO>lambdaQuery()
                                .eq(BrokerageUserDO::getBindUserId, bindUserId))
                .stream()
                .map(BrokerageUserDO::getId)
                .toList();
    }
}
