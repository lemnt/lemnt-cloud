package com.lemnt.mall.application.product.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.product.converter.ProductSpecDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpecValueDTO;
import com.lemnt.mall.application.product.qry.ProductSpecValuePageQry;
import com.lemnt.mall.domain.product.gateway.ProductSpecValueGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSpecFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 规格字典分页查询。 */
@Component
@RequiredArgsConstructor
public class ProductSpecValuePageQueryExe {
    private final ProductSpecValueGateway productSpecValueGateway;

    /** 分页查询字典，可供规格选择器按关键词筛选。 */
    public PageResult<ProductSpecValueDTO> execute(ProductSpecValuePageQry qry) {
        if (qry == null || qry.pageNum() < 1 || qry.pageNum() > 1000000 || qry.pageSize() < 1 || qry.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        if (qry.specId() != null) {
            ProductCatalogValidation.id(qry.specId());
        }
        String name = qry.name() == null || qry.name().isBlank() ? null : ProductCatalogValidation.text(qry.name(), 100, "名称");
        var page = productSpecValueGateway.page(new ProductSpecFilter(name, qry.specId(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(ProductSpecDTOConverter::value).toList(), page.getTotal());
    }
}
