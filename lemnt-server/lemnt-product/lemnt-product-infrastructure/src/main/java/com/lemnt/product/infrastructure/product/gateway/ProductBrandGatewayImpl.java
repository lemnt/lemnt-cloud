package com.lemnt.product.infrastructure.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.product.domain.product.gateway.ProductBrandGateway;
import com.lemnt.product.domain.product.model.ProductBrand;
import com.lemnt.product.domain.product.model.ProductBrandFilter;
import com.lemnt.product.infrastructure.product.converter.ProductCatalogConverter;
import com.lemnt.product.infrastructure.product.dataobject.ProductBrandDO;
import com.lemnt.product.infrastructure.product.mapper.ProductBrandMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * ProductBrand 表持久化实现。
 */
@Component
@RequiredArgsConstructor
public class ProductBrandGatewayImpl implements ProductBrandGateway {
    private final ProductBrandMapper brandMapper;

    @Override
    public PageResult<ProductBrand> page(ProductBrandFilter filter) {
        var page =
            brandMapper.selectPage(
                new Page<>(filter.pageNum(), filter.pageSize()),
                Wrappers.lambdaQuery(ProductBrandDO.class)
                    .like(filter.name() != null, ProductBrandDO::getName, filter.name())
                    .eq(filter.status() != null, ProductBrandDO::getStatus, filter.status())
                    .orderByAsc(ProductBrandDO::getSort)
                    .orderByAsc(ProductBrandDO::getId));
        return PageResult.of(
            page.getRecords().stream().map(ProductCatalogConverter::brand).toList(),
            page.getTotal());
    }

    @Override
    public List<ProductBrand> brands() {
        var rows =
            brandMapper.selectList(
                Wrappers.<ProductBrandDO>lambdaQuery()
                    .orderByAsc(ProductBrandDO::getSort)
                    .orderByAsc(ProductBrandDO::getId)
                    .last("LIMIT 1001"));
        if (rows.size() > 1000) {
            throw new IllegalStateException("品牌超过1000条，请先接入分页品牌维护");
        }
        return rows.stream().map(ProductCatalogConverter::brand).toList();
    }

    @Override
    public ProductBrand brand(Long id) {
        return ProductCatalogConverter.brand(brandMapper.selectById(id));
    }

    @Override
    public ProductBrand saveBrand(ProductBrand value) {
        var row = ProductCatalogConverter.brand(value);
        if (value.id() == null) {
            brandMapper.insert(row);
        } else if (brandMapper.update(
            null,
            Wrappers.<ProductBrandDO>lambdaUpdate()
                .set(ProductBrandDO::getName, row.getName())
                .set(ProductBrandDO::getPicUrl, row.getPicUrl())
                .set(ProductBrandDO::getDescription, row.getDescription())
                .set(ProductBrandDO::getSort, row.getSort())
                .set(ProductBrandDO::getStatus, row.getStatus())
                .eq(ProductBrandDO::getId, row.getId()))
            != 1) {
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
