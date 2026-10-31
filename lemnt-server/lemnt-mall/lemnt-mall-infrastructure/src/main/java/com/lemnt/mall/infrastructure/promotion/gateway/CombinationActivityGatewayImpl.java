package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.gateway.CombinationActivityGateway;
import com.lemnt.mall.domain.promotion.model.CombinationActivity;
import com.lemnt.mall.domain.promotion.model.CombinationFilter;
import com.lemnt.mall.infrastructure.promotion.converter.CombinationConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.CombinationActivityDO;
import com.lemnt.mall.infrastructure.promotion.mapper.CombinationActivityMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 拼团活动仅操作活动表。 */
@Component
@RequiredArgsConstructor
public class CombinationActivityGatewayImpl implements CombinationActivityGateway {
    private final CombinationActivityMapper mapper;

    @Override
    public CombinationActivity find(Long id) {
        return CombinationConverter.model(mapper.selectById(id));
    }

    @Override
    public CombinationActivity save(CombinationActivity value) {
        var row = CombinationConverter.row(value);
        if (row.getId() == null) { mapper.insert(row); } else { mapper.updateById(row); }
        return CombinationConverter.model(row);
    }

    @Override
    public boolean close(Long id) {
        return mapper.update(null, Wrappers.<CombinationActivityDO>lambdaUpdate()
                .eq(CombinationActivityDO::getId, id).set(CombinationActivityDO::getStatus, 1)) == 1;
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public PageResult<CombinationActivity> page(CombinationFilter filter) {
        var page = mapper.selectPage(new Page<CombinationActivityDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<CombinationActivityDO>lambdaQuery()
                        .like(filter.name() != null && !filter.name().isBlank(),
                                CombinationActivityDO::getName, filter.name())
                        .eq(filter.status() != null, CombinationActivityDO::getStatus, filter.status())
                        .eq(filter.spuId() != null, CombinationActivityDO::getSpuId, filter.spuId())
                        .orderByDesc(CombinationActivityDO::getId));
        return PageResult.of(page.getRecords().stream().map(CombinationConverter::model).toList(), page.getTotal());
    }

    @Override
    public List<CombinationActivity> enabled() {
        return mapper.selectList(Wrappers.<CombinationActivityDO>lambdaQuery()
                        .eq(CombinationActivityDO::getStatus, 0).orderByDesc(CombinationActivityDO::getId))
                .stream().map(CombinationConverter::model).toList();
    }

    @Override
    public List<CombinationActivity> byIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapper.selectByIds(ids).stream().map(CombinationConverter::model).toList();
    }

    @Override
    public List<CombinationActivity> enabledBySpu(Long spuId) {
        return mapper.selectList(Wrappers.<CombinationActivityDO>lambdaQuery()
                        .eq(CombinationActivityDO::getSpuId, spuId)
                        .eq(CombinationActivityDO::getStatus, 0))
                .stream().map(CombinationConverter::model).toList();
    }
}
