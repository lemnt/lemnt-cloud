package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.ProductSpecUpdateCmd;
import com.lemnt.mall.application.product.converter.ProductSpecDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpecDTO;
import com.lemnt.mall.application.product.service.ProductSpecService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 规格项修改执行器。 */
@Component
@RequiredArgsConstructor
public class ProductSpecUpdateCmdExe {
    private final ProductSpecService productSpecService;

    /** 执行字典写入操作。 */
    public ProductSpecDTO execute(ProductSpecUpdateCmd value) {
        return ProductSpecDTOConverter.property(productSpecService.update(value));
    }
}
