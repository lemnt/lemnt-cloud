package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.gateway.DiyPageGateway;
import com.lemnt.mall.domain.promotion.model.DiyPage;
import com.lemnt.mall.domain.promotion.model.DiyPageFilter;
import com.lemnt.mall.infrastructure.promotion.converter.DiyConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.DiyPageDO;
import com.lemnt.mall.infrastructure.promotion.mapper.DiyPageMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 装修页面仅操作对应表。 */
@Component
@RequiredArgsConstructor
public class DiyPageGatewayImpl implements DiyPageGateway {
    private final DiyPageMapper mapper;

    @Override
    public DiyPage find(Long id) {
        return DiyConverter.model(mapper.selectById(id));
    }

    @Override
    public DiyPage findByName(Long templateId, String name) {
        return DiyConverter.model(mapper.selectOne(Wrappers.<DiyPageDO>lambdaQuery()
                .eq(DiyPageDO::getTemplateId, templateId).eq(DiyPageDO::getName, name).last("LIMIT 1")));
    }

    @Override
    public List<DiyPage> byTemplateId(Long templateId) {
        return mapper.selectList(Wrappers.<DiyPageDO>lambdaQuery()
                        .eq(DiyPageDO::getTemplateId, templateId).orderByAsc(DiyPageDO::getId))
                .stream().map(DiyConverter::model).toList();
    }

    @Override
    public DiyPage save(DiyPage value) {
        var row = DiyConverter.row(value);
        if (row.getId() == null) {
            mapper.insert(row);
        } else {
            mapper.updateById(row);
        }
        return DiyConverter.model(row);
    }

    @Override
    public PageResult<DiyPage> page(DiyPageFilter filter) {
        var page = mapper.selectPage(new Page<DiyPageDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<DiyPageDO>lambdaQuery()
                        .eq(filter.templateId() != null, DiyPageDO::getTemplateId, filter.templateId())
                        .like(filter.name() != null && !filter.name().isBlank(), DiyPageDO::getName,
                                filter.name())
                        .orderByAsc(DiyPageDO::getId));
        return PageResult.of(page.getRecords().stream().map(DiyConverter::model).toList(),
                page.getTotal());
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
