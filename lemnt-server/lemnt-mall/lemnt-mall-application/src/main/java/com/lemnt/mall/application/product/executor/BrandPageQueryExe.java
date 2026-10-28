package com.lemnt.mall.application.product.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.mall.application.product.dto.BrandDTO;
import com.lemnt.mall.application.product.qry.BrandPageQry;
import com.lemnt.mall.domain.product.gateway.BrandGateway;
import com.lemnt.mall.domain.product.model.BrandFilter;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 品牌分页管理。 */
@Component
@RequiredArgsConstructor
public class BrandPageQueryExe {
    private final BrandGateway brandGateway;

    /** 支持超过旧列表1000条上限的品牌分页。 */
    public PageResult<BrandDTO> execute(BrandPageQry qry) {
        if (qry == null || qry.pageNum() < 1 || qry.pageNum() > 1000000 || qry.pageSize() < 1 || qry.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        String name = qry.name() == null || qry.name().isBlank() ? null : ProductCatalogValidation.text(qry.name(), 100, "品牌名");
        String status = qry.status() == null || qry.status().isBlank() ? null : ProductCatalogValidation.status(qry.status());
        var page = brandGateway.page(new BrandFilter(name, status, qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(ProductCatalogDTOConverter::brand).toList(), page.getTotal());
    }
}
