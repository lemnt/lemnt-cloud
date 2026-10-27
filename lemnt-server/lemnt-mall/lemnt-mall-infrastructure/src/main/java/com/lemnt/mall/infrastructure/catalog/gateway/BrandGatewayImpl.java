package com.lemnt.mall.infrastructure.catalog.gateway;

import com.lemnt.mall.domain.catalog.gateway.BrandGateway;
import com.lemnt.mall.domain.catalog.model.Brand;
import com.lemnt.mall.infrastructure.catalog.converter.CatalogConverter;
import com.lemnt.mall.infrastructure.catalog.mapper.BrandMapper;

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
    public List<Brand> brands() {
        var rows = brandMapper.brands();
        if (rows.size() > 1000) {
            throw new IllegalStateException("品牌超过1000条，请先接入分页品牌维护");
        }
        return rows.stream().map(CatalogConverter::brand).toList();
    }

    @Override
    public Brand brand(Long id) {
        return CatalogConverter.brand(brandMapper.brand(id));
    }

    @Override
    public Brand saveBrand(Brand value) {
        var row = CatalogConverter.brand(value);
        if (value.id() == null) {
            brandMapper.insertBrand(row);
        }
        else if (brandMapper.updateBrand(row) != 1) {
            throw new IllegalArgumentException("品牌不存在");
        }
        return CatalogConverter.brand(row);
    }

    @Override
    public boolean deleteBrand(Long id) {
        try {
            return brandMapper.deleteBrand(id) == 1;
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("品牌仍被商品引用", ex);
        }
    }
}
