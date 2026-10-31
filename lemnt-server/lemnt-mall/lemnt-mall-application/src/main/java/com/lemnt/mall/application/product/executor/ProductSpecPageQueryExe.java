package com.lemnt.mall.application.product.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.product.converter.ProductSpecDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpecDTO;
import com.lemnt.mall.application.product.qry.ProductSpecPageQry;
import com.lemnt.mall.domain.product.gateway.ProductSpecGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSpecFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 规格字典分页查询。 */
@Component
@RequiredArgsConstructor
public class ProductSpecPageQueryExe {
    private final ProductSpecGateway productSpecGateway;

    /** 分页查询字典，可供规格选择器按关键词筛选。 */
    public PageResult<ProductSpecDTO> execute(ProductSpecPageQry qry) {
        if (qry == null || qry.pageNum() < 1 || qry.pageNum() > 1000000 || qry.pageSize() < 1 || qry.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        if (qry.createTimeStart() != null && qry.createTimeEnd() != null
                && qry.createTimeStart().isAfter(qry.createTimeEnd())) {
            throw new IllegalArgumentException("创建时间范围不合法");
        }
        String name = qry.name() == null || qry.name().isBlank() ? null : ProductCatalogValidation.text(qry.name(), 64, "名称");
        var page = productSpecGateway.page(new ProductSpecFilter(name, null, qry.pageNum(), qry.pageSize(), qry.createTimeStart(), qry.createTimeEnd()));
        return PageResult.of(page.getRecords().stream().map(ProductSpecDTOConverter::property).toList(), page.getTotal());
    }
}
