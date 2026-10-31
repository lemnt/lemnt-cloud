package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.gateway.SeckillConfigGateway;
import com.lemnt.mall.domain.promotion.model.SeckillConfig;
import com.lemnt.mall.domain.promotion.model.SeckillConfigFilter;
import com.lemnt.mall.infrastructure.promotion.converter.SeckillConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.SeckillConfigDO;
import com.lemnt.mall.infrastructure.promotion.mapper.SeckillConfigMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 秒杀时段仅操作对应表。 */
@Component
@RequiredArgsConstructor
public class SeckillConfigGatewayImpl implements SeckillConfigGateway {
    private final SeckillConfigMapper mapper;

    @Override
    public SeckillConfig find(Long id) {
        return SeckillConverter.model(mapper.selectById(id));
    }

    @Override
    public SeckillConfig save(SeckillConfig value) {
        var row = SeckillConverter.row(value);
        if (row.getId() == null) { mapper.insert(row); } else { mapper.updateById(row); }
        return SeckillConverter.model(row);
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        return mapper.update(null, Wrappers.<SeckillConfigDO>lambdaUpdate()
                .eq(SeckillConfigDO::getId, id).set(SeckillConfigDO::getStatus, status)) == 1;
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public List<SeckillConfig> all() {
        return mapper.selectList(Wrappers.<SeckillConfigDO>lambdaQuery()
                        .orderByAsc(SeckillConfigDO::getStartTime))
                .stream().map(SeckillConverter::model).toList();
    }

    @Override
    public List<SeckillConfig> byStatus(Integer status) {
        return mapper.selectList(Wrappers.<SeckillConfigDO>lambdaQuery()
                        .eq(SeckillConfigDO::getStatus, status).orderByAsc(SeckillConfigDO::getStartTime))
                .stream().map(SeckillConverter::model).toList();
    }

    @Override
    public List<SeckillConfig> byIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapper.selectByIds(ids).stream().map(SeckillConverter::model).toList();
    }

    @Override
    public PageResult<SeckillConfig> page(SeckillConfigFilter filter) {
        var page = mapper.selectPage(new Page<SeckillConfigDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<SeckillConfigDO>lambdaQuery()
                        .like(filter.name() != null && !filter.name().isBlank(),
                                SeckillConfigDO::getName, filter.name())
                        .eq(filter.status() != null, SeckillConfigDO::getStatus, filter.status())
                        .orderByAsc(SeckillConfigDO::getStartTime));
        return PageResult.of(page.getRecords().stream().map(SeckillConverter::model).toList(), page.getTotal());
    }

    @Override
    public SeckillConfig lockById(Long id) {
        return SeckillConverter.model(mapper.lockAll().stream()
                .filter(row -> row.getId().equals(id)).findFirst().orElse(null));
    }
}
