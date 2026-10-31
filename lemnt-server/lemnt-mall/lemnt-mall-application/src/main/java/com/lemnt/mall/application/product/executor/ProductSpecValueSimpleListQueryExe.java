package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.converter.ProductSpecDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpecValueDTO;
import com.lemnt.mall.application.product.qry.ProductSpecValueSimpleListQry;
import com.lemnt.mall.domain.product.gateway.ProductSpecValueGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSpecFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 规格字典选择查询。 */
@Component
@RequiredArgsConstructor
public class ProductSpecValueSimpleListQueryExe {
    private final ProductSpecValueGateway productSpecValueGateway;

    /** 限定选择条数，规格值选择必须指定真实规格项。 */
    public List<ProductSpecValueDTO> execute(ProductSpecValueSimpleListQry qry) {
        if (qry == null || qry.limit() < 1 || qry.limit() > 100) {
            throw new IllegalArgumentException("选择条数须为1至100");
        }
        ProductCatalogValidation.id(qry.specId());
        String name = qry.name() == null || qry.name().isBlank() ? null : ProductCatalogValidation.text(qry.name(), 100, "名称");
        return productSpecValueGateway.page(new ProductSpecFilter(name, qry.specId(), 1, qry.limit(), null, null))
                .getRecords().stream().map(ProductSpecDTOConverter::value).toList();
    }
}
