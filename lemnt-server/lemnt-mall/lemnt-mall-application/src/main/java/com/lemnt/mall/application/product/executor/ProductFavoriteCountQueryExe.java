package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.qry.ProductFavoriteCountQry;
import com.lemnt.mall.application.product.service.ProductFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** count执行器。 */
@Component
@RequiredArgsConstructor
public class ProductFavoriteCountQueryExe {
    private final ProductFavoriteService service;

    /** 按明确的用例边界执行。 */
    public long execute(ProductFavoriteCountQry value) {
        return service.count(value.userId());
    }
}
