package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.service.ProductSpecService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 规格值删除执行器。 */
@Component
@RequiredArgsConstructor
public class ProductSpecValueDeleteCmdExe {
    private final ProductSpecService productSpecService;

    /** 执行字典写入操作。 */
    public boolean execute(Long value) {
        return productSpecService.deleteValue(value);
    }
}
