package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.gateway.BrandGateway;
import com.lemnt.mall.domain.product.model.Brand;
import com.lemnt.mall.domain.product.model.BrandFilter;
import com.lemnt.mall.infrastructure.product.converter.ProductCatalogConverter;
import com.lemnt.mall.infrastructure.product.dataobject.BrandDO;
import com.lemnt.mall.infrastructure.product.mapper.BrandMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** Brand 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class BrandGatewayImpl implements BrandGateway {
    private final BrandMapper brandMapper;

    @Override
    public PageResult<Brand> page(BrandFilter filter) {
        var page = brandMapper.selectPage(new Page<>(
                filter.pageNum(), filter.pageSize()), Wrappers
                .lambdaQuery(BrandDO.class)
                .like(filter.name() != null, BrandDO::getName, filter.name())
                .eq(filter.status() != null, BrandDO::getStatus, filter.status())
                .orderByAsc(BrandDO::getSort)
                .orderByAsc(BrandDO::getId));
        return PageResult.of(
                page.getRecords().stream().map(ProductCatalogConverter::brand).toList(), page.getTotal());
    }

    @Override
    public List<Brand> brands() {
        var rows = brandMapper.selectList(Wrappers.<BrandDO>lambdaQuery()
                .orderByAsc(BrandDO::getSort)
                .orderByAsc(BrandDO::getId)
                .last("LIMIT 1001"));
        if (rows.size() > 1000) {
            throw new IllegalStateException("品牌超过1000条，请先接入分页品牌维护");
        }
        return rows.stream().map(ProductCatalogConverter::brand).toList();
    }

    @Override
    public Brand brand(Long id) {
        return ProductCatalogConverter.brand(brandMapper.selectById(id));
    }

    @Override
    public Brand saveBrand(Brand value) {
        var row = ProductCatalogConverter.brand(value);
        if (value.id() == null) {
            brandMapper.insert(row);
        }
        else if (brandMapper.update(null, Wrappers.<BrandDO>lambdaUpdate()
                .set(BrandDO::getName, row.getName())
                .set(BrandDO::getPicUrl, row.getPicUrl())
                .set(BrandDO::getDescription, row.getDescription())
                .set(BrandDO::getSort, row.getSort())
                .set(BrandDO::getStatus, row.getStatus())
                .eq(BrandDO::getId, row.getId())) != 1) {
            throw new IllegalArgumentException("品牌不存在");
        }
        return ProductCatalogConverter.brand(row);
    }

    @Override
    public boolean deleteBrand(Long id) {
        try {
            return brandMapper.deleteById(id) == 1;
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("品牌仍被商品引用", ex);
        }
    }
}
