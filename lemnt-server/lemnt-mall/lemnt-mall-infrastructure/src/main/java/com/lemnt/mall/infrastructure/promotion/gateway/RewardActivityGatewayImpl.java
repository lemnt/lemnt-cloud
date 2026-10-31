package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.gateway.RewardActivityGateway;
import com.lemnt.mall.domain.promotion.model.ActivityFilter;
import com.lemnt.mall.domain.promotion.model.RewardActivity;
import com.lemnt.mall.infrastructure.promotion.converter.PromotionConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.RewardActivityDO;
import com.lemnt.mall.infrastructure.promotion.mapper.RewardActivityMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 仅操作对应活动主表，简单查询使用 Lambda。 */
@Component
@RequiredArgsConstructor
public class RewardActivityGatewayImpl implements RewardActivityGateway {
    private final RewardActivityMapper mapper;

    @Override
    public RewardActivity find(Long id) {
        return PromotionConverter.model(mapper.selectById(id));
    }

    @Override
    public RewardActivity save(RewardActivity value) {
        var row = PromotionConverter.row(value);
        if (row.getId() == null) { mapper.insert(row); } else {
            mapper.updateById(row);
        }
        return PromotionConverter.model(row);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public PageResult<RewardActivity> page(ActivityFilter filter) {
        var page = mapper.selectPage(new Page<RewardActivityDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<RewardActivityDO>lambdaQuery()
                        .like(filter.name() != null && !filter.name().isBlank(), RewardActivityDO::getName,
                            filter.name())
                        .eq(filter.status() != null, RewardActivityDO::getStatus, filter.status())
                        .orderByDesc(RewardActivityDO::getId));
        return PageResult.of(page.getRecords().stream().map(PromotionConverter::model).toList(), page.getTotal());
    }

    @Override
    public List<RewardActivity> enabled() {
        return mapper.selectList(Wrappers.<RewardActivityDO>lambdaQuery()
                .eq(RewardActivityDO::getStatus, 0).orderByDesc(RewardActivityDO::getId))
                .stream().map(PromotionConverter::model).toList();
    }
}
