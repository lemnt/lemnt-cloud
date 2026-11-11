package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.cmd.ProductSpecCreateCmd;
import com.lemnt.product.application.product.converter.ProductSpecDTOConverter;
import com.lemnt.product.application.product.dto.ProductSpecDTO;
import com.lemnt.product.application.product.service.ProductSpecService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 规格项新增执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductSpecCreateCmdExe {
    private final ProductSpecService productSpecService;

    /**
     * 执行字典写入操作。
     */
    public ProductSpecDTO execute(ProductSpecCreateCmd value) {
        return ProductSpecDTOConverter.property(productSpecService.create(value));
    }
}
