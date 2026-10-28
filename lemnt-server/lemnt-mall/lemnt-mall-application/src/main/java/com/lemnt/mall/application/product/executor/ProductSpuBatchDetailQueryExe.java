package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpuDTO;
import com.lemnt.mall.application.product.service.ProductManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 商品批量详情查询。 */
@Component
@RequiredArgsConstructor
public class ProductSpuBatchDetailQueryExe {
    private final ProductManagementService catalogManagementService;

    /** 批量读取真实详情，不伪造缺失商品。 */
    public List<ProductSpuDTO> execute(List<Long> ids) {
        return catalogManagementService.batchDetails(ids).stream().map(ProductCatalogDTOConverter::spu).toList();
    }
}
