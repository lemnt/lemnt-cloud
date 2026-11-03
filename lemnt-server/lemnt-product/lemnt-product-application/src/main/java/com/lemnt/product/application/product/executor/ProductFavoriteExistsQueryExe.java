package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.qry.ProductFavoriteExistsQry;
import com.lemnt.product.application.product.service.ProductFavoriteService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** exists执行器。 */
@Component
@RequiredArgsConstructor
public class ProductFavoriteExistsQueryExe {
    private final ProductFavoriteService service;

    /** 按明确的用例边界执行。 */
    public boolean execute(ProductFavoriteExistsQry value) {
        return service.exists(value.userId(), value.spuId());
    }
}
