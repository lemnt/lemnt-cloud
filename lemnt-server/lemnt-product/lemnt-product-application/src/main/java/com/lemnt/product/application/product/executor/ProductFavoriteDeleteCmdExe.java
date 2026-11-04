package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.cmd.ProductFavoriteDeleteCmd;
import com.lemnt.product.application.product.service.ProductFavoriteService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * remove执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductFavoriteDeleteCmdExe {
    private final ProductFavoriteService service;

    /**
     * 按明确的用例边界执行。
     */
    public boolean execute(ProductFavoriteDeleteCmd value) {
        return service.remove(value.userId(), value.spuId());
    }
}
