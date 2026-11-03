package com.lemnt.product.application.product.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.product.application.product.dto.ProductFavoriteDTO;
import com.lemnt.product.application.product.qry.ProductFavoritePageQry;
import com.lemnt.product.application.product.service.ProductFavoriteService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Favoritepage执行器。 */
@Component
@RequiredArgsConstructor
public class ProductFavoritePageQueryExe {
    private final ProductFavoriteService service;

    /** 委托领域应用服务完成本次用例。 */
    public PageResult<ProductFavoriteDTO> execute(ProductFavoritePageQry value) {
        return service.page(value);
    }
}
