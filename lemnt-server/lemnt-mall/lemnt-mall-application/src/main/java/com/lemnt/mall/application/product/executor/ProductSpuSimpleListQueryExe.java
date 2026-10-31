package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpuDTO;
import com.lemnt.mall.application.product.qry.ProductSpuSimpleListQry;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 在售商品选择查询。 */
@Component
@RequiredArgsConstructor
public class ProductSpuSimpleListQueryExe {
    private final ProductSpuGateway productSpuGateway;

    /** 显式限制查询条数，避免隐含的无上限下拉列表。 */
    public List<ProductSpuDTO> execute(ProductSpuSimpleListQry qry) {
        if (qry == null || qry.limit() < 1 || qry.limit() > 100) {
            throw new IllegalArgumentException("选择条数须为1至100");
        }
        String name = qry.name() == null || qry.name().isBlank() ? null : ProductCatalogValidation.text(qry.name(), 200, "名称");
        return productSpuGateway.simpleList(name, qry.limit()).stream().map(ProductCatalogDTOConverter::spu).toList();
    }
}
