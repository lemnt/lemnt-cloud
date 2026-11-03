package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.cmd.ProductFavoriteCreateCmd;
import com.lemnt.product.application.product.service.ProductFavoriteService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

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
