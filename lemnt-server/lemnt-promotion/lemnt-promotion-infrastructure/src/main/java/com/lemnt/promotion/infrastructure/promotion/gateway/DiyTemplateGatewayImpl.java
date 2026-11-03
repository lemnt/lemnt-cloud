package com.lemnt.promotion.infrastructure.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.promotion.domain.promotion.gateway.DiyTemplateGateway;
import com.lemnt.promotion.domain.promotion.model.DiyTemplate;
import com.lemnt.promotion.domain.promotion.model.DiyTemplateFilter;
import com.lemnt.promotion.infrastructure.promotion.converter.DiyConverter;
import com.lemnt.promotion.infrastructure.promotion.dataobject.DiyTemplateDO;
import com.lemnt.promotion.infrastructure.promotion.mapper.DiyTemplateMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修模板仅操作对应表。 */
@Component
@RequiredArgsConstructor
public class DiyTemplateGatewayImpl implements DiyTemplateGateway {
    private final DiyTemplateMapper mapper;

    @Override
    public DiyTemplate find(Long id) {
        return DiyConverter.model(mapper.selectById(id));
    }

    @Override
    public DiyTemplate findByName(String name) {
        return DiyConverter.model(
                mapper.selectOne(
                        Wrappers.<DiyTemplateDO>lambdaQuery()
                                .eq(DiyTemplateDO::getName, name)
                                .last("LIMIT 1")));
    }

    @Override
    public DiyTemplate findUsed() {
        return DiyConverter.model(
                mapper.selectOne(
                        Wrappers.<DiyTemplateDO>lambdaQuery()
                                .eq(DiyTemplateDO::getUsed, true)
                                .last("LIMIT 1")));
    }

    @Override
    public DiyTemplate save(DiyTemplate value) {
        var row = DiyConverter.row(value);
        if (row.getId() == null) {
            mapper.insert(row);
        } else {
            mapper.updateById(row);
        }
        return DiyConverter.model(row);
    }

    @Override
    public PageResult<DiyTemplate> page(DiyTemplateFilter filter) {
        var page =
                mapper.selectPage(
                        new Page<DiyTemplateDO>(filter.pageNum(), filter.pageSize()),
                        Wrappers.<DiyTemplateDO>lambdaQuery()
                                .like(
                                        filter.name() != null && !filter.name().isBlank(),
                                        DiyTemplateDO::getName,
                                        filter.name())
                                .eq(filter.used() != null, DiyTemplateDO::getUsed, filter.used())
                                .orderByDesc(DiyTemplateDO::getId));
        return PageResult.of(
                page.getRecords().stream().map(DiyConverter::model).toList(), page.getTotal());
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
