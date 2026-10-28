package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.gateway.ProductSpecValueGateway;
import com.lemnt.mall.domain.product.model.ProductSpecFilter;
import com.lemnt.mall.domain.product.model.ProductSpecValue;
import com.lemnt.mall.infrastructure.product.converter.ProductSpecValueConverter;
import com.lemnt.mall.infrastructure.product.dataobject.ProductSpecValueDO;
import com.lemnt.mall.infrastructure.product.mapper.ProductSpecValueMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** mall_product_spec_value表存储实现。 */
@Component
@RequiredArgsConstructor
public class ProductSpecValueGatewayImpl implements ProductSpecValueGateway {
    private final ProductSpecValueMapper productSpecValueMapper;
    @Override
    public ProductSpecValue find(Long id) {
        return ProductSpecValueConverter.toModel(productSpecValueMapper.selectById(id));
    }

    @Override
    public ProductSpecValue lock(Long id) {
        return ProductSpecValueConverter.toModel(productSpecValueMapper.lock(id));
    }

    @Override
    public ProductSpecValue save(ProductSpecValue value) {
        var row = ProductSpecValueConverter.toDO(value);
        if (row.getId() == null) {
            productSpecValueMapper.insert(row);
        }
        else if (productSpecValueMapper.update(null,
            Wrappers.lambdaUpdate(ProductSpecValueDO.class).eq(ProductSpecValueDO::getId, row.getId())
                .set(ProductSpecValueDO::getName, row.getName()).set(ProductSpecValueDO::getRemark,
                    row.getRemark())) != 1) {
            throw new IllegalArgumentException("字典记录不存在");
        }
        return find(row.getId());
    }

    @Override
    public boolean delete(Long id) {
        return productSpecValueMapper.deleteById(id) == 1;
    }

    @Override
    public PageResult<ProductSpecValue> page(ProductSpecFilter filter) {
        var page = productSpecValueMapper.selectPage(new Page<>(filter.pageNum(), filter.pageSize()),
                Wrappers.lambdaQuery(ProductSpecValueDO.class).like(filter.name() != null,
                    ProductSpecValueDO::getName, filter.name())
                .eq(filter.specId() != null, ProductSpecValueDO::getSpecId,
                    filter.specId()).orderByAsc(ProductSpecValueDO::getId));
        return PageResult.of(page.getRecords().stream().map(ProductSpecValueConverter::toModel).toList(),
            page.getTotal());
    }
}
