package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.gateway.PointActivityGateway;
import com.lemnt.mall.domain.promotion.model.PointActivity;
import com.lemnt.mall.domain.promotion.model.PointFilter;
import com.lemnt.mall.infrastructure.promotion.converter.PointConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.PointActivityDO;
import com.lemnt.mall.infrastructure.promotion.mapper.PointActivityMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 积分商城活动仅操作活动表。 */
@Component
@RequiredArgsConstructor
public class PointActivityGatewayImpl implements PointActivityGateway {
    private final PointActivityMapper mapper;

    @Override
    public PointActivity find(Long id) {
        return PointConverter.model(mapper.selectById(id));
    }

    @Override
    public PointActivity save(PointActivity value) {
        var row = PointConverter.row(value);
        if (row.getId() == null) { mapper.insert(row); } else { mapper.updateById(row); }
        return PointConverter.model(row);
    }

    @Override
    public boolean close(Long id) {
        return mapper.update(null, Wrappers.<PointActivityDO>lambdaUpdate()
                .eq(PointActivityDO::getId, id).set(PointActivityDO::getStatus, 1)) == 1;
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public PageResult<PointActivity> page(PointFilter filter) {
        var page = mapper.selectPage(new Page<PointActivityDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<PointActivityDO>lambdaQuery()
                        .eq(filter.status() != null, PointActivityDO::getStatus, filter.status())
                        .orderByDesc(PointActivityDO::getSort).orderByDesc(PointActivityDO::getId));
        return PageResult.of(page.getRecords().stream().map(PointConverter::model).toList(), page.getTotal());
    }

    @Override
    public List<PointActivity> enabled() {
        return mapper.selectList(Wrappers.<PointActivityDO>lambdaQuery()
                        .eq(PointActivityDO::getStatus, 0)
                        .orderByDesc(PointActivityDO::getSort).orderByDesc(PointActivityDO::getId))
                .stream().map(PointConverter::model).toList();
    }

    @Override
    public PointActivity lockById(Long id) {
        return PointConverter.model(mapper.lockById(id));
    }

    @Override
    public boolean decreaseStock(Long id, int count) {
        return mapper.update(null, Wrappers.<PointActivityDO>lambdaUpdate()
                .eq(PointActivityDO::getId, id).ge(PointActivityDO::getStock, count)
                .setDecrBy(PointActivityDO::getStock, count)) == 1;
    }

    @Override
    public boolean increaseStock(Long id, int count) {
        return mapper.update(null, Wrappers.<PointActivityDO>lambdaUpdate()
                .eq(PointActivityDO::getId, id).setIncrBy(PointActivityDO::getStock, count)) == 1;
    }
}
