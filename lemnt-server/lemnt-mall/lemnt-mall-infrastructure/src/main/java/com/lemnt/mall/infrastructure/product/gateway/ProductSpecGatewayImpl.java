package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.gateway.ProductSpecGateway;
import com.lemnt.mall.domain.product.model.ProductSpec;
import com.lemnt.mall.domain.product.model.ProductSpecFilter;
import com.lemnt.mall.infrastructure.product.converter.ProductSpecConverter;
import com.lemnt.mall.infrastructure.product.dataobject.ProductSpecDO;
import com.lemnt.mall.infrastructure.product.mapper.ProductSpecMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** mall_product_spec表存储实现。 */
@Component
@RequiredArgsConstructor
public class ProductSpecGatewayImpl implements ProductSpecGateway {
    private final ProductSpecMapper productSpecMapper;
    @Override
    public ProductSpec find(Long id) {
        return ProductSpecConverter.toModel(productSpecMapper.selectById(id));
    }

    @Override
    public ProductSpec lock(Long id) {
        return ProductSpecConverter.toModel(productSpecMapper.lock(id));
    }

    @Override
    public ProductSpec save(ProductSpec value) {
        var row = ProductSpecConverter.toDO(value);
        if (row.getId() == null) {
            productSpecMapper.insert(row);
        }
        else if (productSpecMapper.update(null, Wrappers.lambdaUpdate(ProductSpecDO.class).eq(ProductSpecDO::getId,
            row.getId())
                .set(ProductSpecDO::getName, row.getName()).set(ProductSpecDO::getRemark,
                    row.getRemark())) != 1) {
            throw new IllegalArgumentException("字典记录不存在");
        }
        return find(row.getId());
    }

    @Override
    public boolean delete(Long id) {
        return productSpecMapper.deleteById(id) == 1;
    }

    @Override
    public PageResult<ProductSpec> page(ProductSpecFilter filter) {
        var page = productSpecMapper.selectPage(new Page<>(filter.pageNum(), filter.pageSize()),
                Wrappers.lambdaQuery(ProductSpecDO.class).like(filter.name() != null,
                    ProductSpecDO::getName, filter.name())
                .orderByAsc(ProductSpecDO::getId));
        return PageResult.of(page.getRecords().stream().map(ProductSpecConverter::toModel).toList(),
            page.getTotal());
    }
}
