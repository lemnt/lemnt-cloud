package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.service.ProductManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 商品恢复执行器。 */
@Component
@RequiredArgsConstructor
public class ProductSpuRestoreCmdExe {
    private final ProductManagementService catalogManagementService;

    /** 执行商品管理写操作。 */
    public boolean execute(Long value) {
        return catalogManagementService.restore(value);
    }
}
