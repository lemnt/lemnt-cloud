package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.ProductSpecValueUpdateCmd;
import com.lemnt.mall.application.product.converter.ProductSpecDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpecValueDTO;
import com.lemnt.mall.application.product.service.ProductSpecService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 规格值修改执行器。 */
@Component
@RequiredArgsConstructor
public class ProductSpecValueUpdateCmdExe {
    private final ProductSpecService productSpecService;

    /** 执行字典写入操作。 */
    public ProductSpecValueDTO execute(ProductSpecValueUpdateCmd value) {
        return ProductSpecDTOConverter.value(productSpecService.updateValue(value));
    }
}
