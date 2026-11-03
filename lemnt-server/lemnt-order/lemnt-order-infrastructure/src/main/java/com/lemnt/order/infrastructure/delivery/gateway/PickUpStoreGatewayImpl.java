package com.lemnt.order.infrastructure.delivery.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.order.domain.delivery.gateway.PickUpStoreGateway;
import com.lemnt.order.domain.delivery.model.PickUpStore;
import com.lemnt.order.domain.delivery.model.PickUpStoreFilter;
import com.lemnt.order.infrastructure.delivery.converter.PickUpStoreConverter;
import com.lemnt.order.infrastructure.delivery.dataobject.PickUpStoreDO;
import com.lemnt.order.infrastructure.delivery.mapper.PickUpStoreMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 自提门店仅操作对应表。 */
@Component
@RequiredArgsConstructor
public class PickUpStoreGatewayImpl implements PickUpStoreGateway {
    private final PickUpStoreMapper mapper;

    @Override
    public PickUpStore find(Long id) {
        return PickUpStoreConverter.model(mapper.selectById(id));
    }

    @Override
    public PickUpStore save(PickUpStore value) {
        var row = PickUpStoreConverter.row(value);
        if (row.getId() == null) {
            mapper.insert(row);
        } else {
            mapper.updateById(row);
        }
        return PickUpStoreConverter.model(row);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public PageResult<PickUpStore> page(PickUpStoreFilter filter) {
        var page =
                mapper.selectPage(
                        new Page<PickUpStoreDO>(filter.pageNum(), filter.pageSize()),
                        Wrappers.<PickUpStoreDO>lambdaQuery()
                                .like(
                                        filter.name() != null && !filter.name().isBlank(),
                                        PickUpStoreDO::getName,
                                        filter.name())
                                .eq(
                                        filter.status() != null,
                                        PickUpStoreDO::getStatus,
                                        filter.status())
                                .orderByDesc(PickUpStoreDO::getId));
        return PageResult.of(
                page.getRecords().stream().map(PickUpStoreConverter::model).toList(),
                page.getTotal());
    }

    @Override
    public List<PickUpStore> enabled() {
        return mapper
                .selectList(
                        Wrappers.<PickUpStoreDO>lambdaQuery()
                                .eq(PickUpStoreDO::getStatus, 0)
                                .orderByAsc(PickUpStoreDO::getId))
                .stream()
                .map(PickUpStoreConverter::model)
                .toList();
    }
}
