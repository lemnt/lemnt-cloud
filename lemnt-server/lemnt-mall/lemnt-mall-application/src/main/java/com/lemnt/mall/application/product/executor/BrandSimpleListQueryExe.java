package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.mall.application.product.dto.BrandDTO;
import com.lemnt.mall.application.product.qry.BrandSimpleListQry;
import com.lemnt.mall.domain.product.gateway.BrandGateway;
import com.lemnt.mall.domain.product.model.BrandFilter;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 启用品牌选择查询。 */
@Component
@RequiredArgsConstructor
public class BrandSimpleListQueryExe {
    private final BrandGateway brandGateway;

    /** 有界关键词查询，避免品牌超过1000条后下拉选项失效。 */
    public List<BrandDTO> execute(BrandSimpleListQry qry) {
        if (qry == null || qry.limit() < 1 || qry.limit() > 100) {
            throw new IllegalArgumentException("选择条数须为1至100");
        }
        String name = qry.name() == null || qry.name().isBlank() ? null : ProductCatalogValidation.text(qry.name(), 100, "品牌名");
        return brandGateway.page(new BrandFilter(name, "ENABLED", 1, qry.limit())).getRecords().stream()
                .map(ProductCatalogDTOConverter::brand).toList();
    }
}
