package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.qry.ProductFavoritePageQry;
import com.lemnt.mall.application.product.service.ProductFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Favoritepage执行器。 */
@Component
@RequiredArgsConstructor
public class ProductFavoritePageQueryExe {
    private final ProductFavoriteService service;

    /** 委托领域应用服务完成本次用例。 */
    public com.lemnt.common.domain.model.PageResult<com.lemnt.mall.application.product.dto.ProductFavoriteDTO> execute(ProductFavoritePageQry value) {
        return service.page(value);
    }
}
