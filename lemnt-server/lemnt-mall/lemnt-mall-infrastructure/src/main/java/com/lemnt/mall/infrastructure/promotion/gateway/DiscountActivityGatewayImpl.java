package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.gateway.DiscountActivityGateway;
import com.lemnt.mall.domain.promotion.model.ActivityFilter;
import com.lemnt.mall.domain.promotion.model.DiscountActivity;
import com.lemnt.mall.infrastructure.promotion.converter.PromotionConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.DiscountActivityDO;
import com.lemnt.mall.infrastructure.promotion.mapper.DiscountActivityMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 仅操作对应活动主表，简单查询使用 Lambda。 */
@Component
@RequiredArgsConstructor
public class DiscountActivityGatewayImpl implements DiscountActivityGateway {
    private final DiscountActivityMapper mapper;

    @Override
    public DiscountActivity find(Long id) {
        return PromotionConverter.model(mapper.selectById(id));
    }

    @Override
    public DiscountActivity save(DiscountActivity value) {
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
    public PageResult<DiscountActivity> page(ActivityFilter filter) {
        var page = mapper.selectPage(new Page<DiscountActivityDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<DiscountActivityDO>lambdaQuery()
                        .like(filter.name() != null && !filter.name().isBlank(), DiscountActivityDO::getName,
                            filter.name())
                        .eq(filter.status() != null, DiscountActivityDO::getStatus, filter.status())
                        .orderByDesc(DiscountActivityDO::getId));
        return PageResult.of(page.getRecords().stream().map(PromotionConverter::model).toList(), page.getTotal());
    }

    @Override
    public List<DiscountActivity> enabled() {
        return mapper.selectList(Wrappers.<DiscountActivityDO>lambdaQuery()
                .eq(DiscountActivityDO::getStatus, 0).orderByDesc(DiscountActivityDO::getId))
                .stream().map(PromotionConverter::model).toList();
    }
}
