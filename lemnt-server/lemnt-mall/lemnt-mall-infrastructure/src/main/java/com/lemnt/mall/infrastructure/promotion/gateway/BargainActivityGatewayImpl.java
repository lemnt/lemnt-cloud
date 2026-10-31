package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.gateway.BargainActivityGateway;
import com.lemnt.mall.domain.promotion.model.BargainActivity;
import com.lemnt.mall.domain.promotion.model.BargainFilter;
import com.lemnt.mall.infrastructure.promotion.converter.BargainConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.BargainActivityDO;
import com.lemnt.mall.infrastructure.promotion.mapper.BargainActivityMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 砍价活动仅操作活动表。 */
@Component
@RequiredArgsConstructor
public class BargainActivityGatewayImpl implements BargainActivityGateway {
    private final BargainActivityMapper mapper;

    @Override
    public BargainActivity find(Long id) {
        return BargainConverter.model(mapper.selectById(id));
    }

    @Override
    public BargainActivity save(BargainActivity value) {
        var row = BargainConverter.row(value);
        if (row.getId() == null) { mapper.insert(row); } else { mapper.updateById(row); }
        return BargainConverter.model(row);
    }

    @Override
    public boolean close(Long id) {
        return mapper.update(null, Wrappers.<BargainActivityDO>lambdaUpdate()
                .eq(BargainActivityDO::getId, id).set(BargainActivityDO::getStatus, 1)) == 1;
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public PageResult<BargainActivity> page(BargainFilter filter) {
        var page = mapper.selectPage(new Page<BargainActivityDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<BargainActivityDO>lambdaQuery()
                        .like(filter.name() != null && !filter.name().isBlank(),
                                BargainActivityDO::getName, filter.name())
                        .eq(filter.status() != null, BargainActivityDO::getStatus, filter.status())
                        .orderByDesc(BargainActivityDO::getId));
        return PageResult.of(page.getRecords().stream().map(BargainConverter::model).toList(), page.getTotal());
    }

    @Override
    public List<BargainActivity> enabled() {
        return mapper.selectList(Wrappers.<BargainActivityDO>lambdaQuery()
                        .eq(BargainActivityDO::getStatus, 0).orderByDesc(BargainActivityDO::getId))
                .stream().map(BargainConverter::model).toList();
    }

    @Override
    public List<BargainActivity> all() {
        return mapper.selectList(Wrappers.<BargainActivityDO>lambdaQuery()
                        .orderByDesc(BargainActivityDO::getId))
                .stream().map(BargainConverter::model).toList();
    }

    @Override
    public boolean decreaseStock(Long id, int count) {
        return mapper.update(null, Wrappers.<BargainActivityDO>lambdaUpdate()
                .eq(BargainActivityDO::getId, id).ge(BargainActivityDO::getStock, count)
                .setDecrBy(BargainActivityDO::getStock, count)) == 1;
    }

    @Override
    public boolean increaseStock(Long id, int count) {
        return mapper.update(null, Wrappers.<BargainActivityDO>lambdaUpdate()
                .eq(BargainActivityDO::getId, id)
                .setIncrBy(BargainActivityDO::getStock, count)) == 1;
    }
}
