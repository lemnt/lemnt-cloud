package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.qry.ProductFavoriteExistsQry;
import com.lemnt.mall.application.product.service.ProductFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
