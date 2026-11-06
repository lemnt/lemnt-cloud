package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.qry.ProductFavoriteCountQry;
import com.lemnt.product.application.product.service.ProductFavoriteService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * count执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductFavoriteCountQueryExe {
    private final ProductFavoriteService service;

    /**
     * 按明确的用例边界执行。
     */
    public long execute(ProductFavoriteCountQry value) {
        return service.count(value.userId());
    }
}
