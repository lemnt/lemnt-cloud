package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.product.application.product.dto.ProductBrandDTO;
import com.lemnt.product.application.product.qry.ProductBrandSimpleListQry;
import com.lemnt.product.domain.product.gateway.ProductBrandGateway;
import com.lemnt.product.domain.product.model.ProductBrandFilter;
import com.lemnt.product.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 启用品牌选择查询。
 */
@Component
@RequiredArgsConstructor
public class ProductBrandSimpleListQueryExe {
    private final ProductBrandGateway brandGateway;

    /**
     * 有界关键词查询，避免品牌超过1000条后下拉选项失效。
     */
    public List<ProductBrandDTO> execute(ProductBrandSimpleListQry qry) {
        if (qry == null || qry.limit() < 1 || qry.limit() > 100) {
            throw new IllegalArgumentException("选择条数须为1至100");
        }
        String name =
            qry.name() == null || qry.name().isBlank()
                ? null
                : ProductCatalogValidation.text(qry.name(), 100, "品牌名");
        return brandGateway
            .page(new ProductBrandFilter(name, "ENABLED", 1, qry.limit()))
            .getRecords()
            .stream()
            .map(ProductCatalogDTOConverter::brand)
            .toList();
    }
}
