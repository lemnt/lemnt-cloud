package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.converter.ProductSpecDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpecDTO;
import com.lemnt.mall.application.product.qry.ProductSpecSimpleListQry;
import com.lemnt.mall.domain.product.gateway.ProductSpecGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSpecFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 规格字典选择查询。 */
@Component
@RequiredArgsConstructor
public class ProductSpecSimpleListQueryExe {
    private final ProductSpecGateway productSpecGateway;

    /** 限定选择条数，规格值选择必须指定真实规格项。 */
    public List<ProductSpecDTO> execute(ProductSpecSimpleListQry qry) {
        if (qry == null || qry.limit() < 1 || qry.limit() > 100) {
            throw new IllegalArgumentException("选择条数须为1至100");
        }
        String name = qry.name() == null || qry.name().isBlank() ? null : ProductCatalogValidation.text(qry.name(), 64, "名称");
        return productSpecGateway.page(new ProductSpecFilter(name, null, 1, qry.limit(), null, null))
                .getRecords().stream().map(ProductSpecDTOConverter::property).toList();
    }
}
