package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.gateway.SeckillActivityGateway;
import com.lemnt.mall.domain.promotion.model.SeckillActivity;
import com.lemnt.mall.domain.promotion.model.SeckillFilter;
import com.lemnt.mall.infrastructure.promotion.converter.SeckillConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.SeckillActivityDO;
import com.lemnt.mall.infrastructure.promotion.mapper.SeckillActivityMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 秒杀活动仅操作活动主表。 */
@Component
@RequiredArgsConstructor
public class SeckillActivityGatewayImpl implements SeckillActivityGateway {
    private final SeckillActivityMapper mapper;

    @Override
    public SeckillActivity find(Long id) {
        return SeckillConverter.model(mapper.selectById(id));
    }

    @Override
    public SeckillActivity save(SeckillActivity value) {
        var row = SeckillConverter.row(value);
        if (row.getId() == null) { mapper.insert(row); } else { mapper.updateById(row); }
        return SeckillConverter.model(row);
    }

    @Override
    public boolean close(Long id) {
        return mapper.update(null, Wrappers.<SeckillActivityDO>lambdaUpdate()
                .eq(SeckillActivityDO::getId, id).set(SeckillActivityDO::getStatus, 1)) == 1;
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public PageResult<SeckillActivity> page(SeckillFilter filter) {
        var page = mapper.selectPageByFilter(new Page<SeckillActivityDO>(filter.pageNum(), filter.pageSize()),
                filter.name(), filter.status(), filter.configId());
        return PageResult.of(page.getRecords().stream().map(SeckillConverter::model).toList(), page.getTotal());
    }

    @Override
    public List<SeckillActivity> enabled() {
        return mapper.selectList(Wrappers.<SeckillActivityDO>lambdaQuery()
                        .eq(SeckillActivityDO::getStatus, 0).orderByDesc(SeckillActivityDO::getId))
                .stream().map(SeckillConverter::model).toList();
    }

    @Override
    public PageResult<SeckillActivity> appPage(Long configId, java.time.LocalDateTime now, int pageNum,
            int pageSize) {
        var page = mapper.selectAppPage(new Page<SeckillActivityDO>(pageNum, pageSize), configId, now);
        return PageResult.of(page.getRecords().stream().map(SeckillConverter::model).toList(), page.getTotal());
    }

    @Override
    public List<SeckillActivity> byConfigAndNow(Long configId, java.time.LocalDateTime now) {
        return mapper.selectByConfigAndNow(configId, now).stream().map(SeckillConverter::model).toList();
    }

    @Override
    public List<SeckillActivity> all() {
        return mapper.selectList(Wrappers.<SeckillActivityDO>lambdaQuery()
                        .orderByDesc(SeckillActivityDO::getId))
                .stream().map(SeckillConverter::model).toList();
    }

    @Override
    public List<SeckillActivity> byIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapper.selectByIds(ids).stream().map(SeckillConverter::model).toList();
    }

    @Override
    public List<SeckillActivity> enabledBySpuIds(List<Long> spuIds) {
        if (spuIds == null || spuIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectEnabledBySpuIds(spuIds).stream().map(SeckillConverter::model).toList();
    }

    @Override
    public SeckillActivity lockById(Long id) {
        return SeckillConverter.model(mapper.lockById(id));
    }

    @Override
    public boolean decreaseStock(Long id, int count) {
        return mapper.update(null, Wrappers.<SeckillActivityDO>lambdaUpdate()
                .eq(SeckillActivityDO::getId, id).ge(SeckillActivityDO::getStock, count)
                .setDecrBy(SeckillActivityDO::getStock, count)) == 1;
    }

    @Override
    public boolean increaseStock(Long id, int count) {
        return mapper.update(null, Wrappers.<SeckillActivityDO>lambdaUpdate()
                .eq(SeckillActivityDO::getId, id).setIncrBy(SeckillActivityDO::getStock, count)) == 1;
    }
}
