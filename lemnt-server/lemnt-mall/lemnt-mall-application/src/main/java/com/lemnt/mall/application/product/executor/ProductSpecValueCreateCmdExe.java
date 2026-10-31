package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.ProductSpecValueCreateCmd;
import com.lemnt.mall.application.product.converter.ProductSpecDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpecValueDTO;
import com.lemnt.mall.application.product.service.ProductSpecService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 规格值新增执行器。 */
@Component
@RequiredArgsConstructor
public class ProductSpecValueCreateCmdExe {
    private final ProductSpecService productSpecService;

    /** 执行字典写入操作。 */
    public ProductSpecValueDTO execute(ProductSpecValueCreateCmd value) {
        return ProductSpecDTOConverter.value(productSpecService.createValue(value));
    }
}
