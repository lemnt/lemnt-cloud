package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.ProductFavoriteCreateCmd;
import com.lemnt.mall.application.product.service.ProductFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Favoritecreate执行器。 */
@Component
@RequiredArgsConstructor
public class ProductFavoriteCreateCmdExe {
    private final ProductFavoriteService service;

    /** 委托领域应用服务完成本次用例。 */
    public Long execute(ProductFavoriteCreateCmd value) {
        return service.create(value);
    }
}
