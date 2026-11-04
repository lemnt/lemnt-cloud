package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.cmd.ProductCommentCreateCmd;
import com.lemnt.product.application.product.converter.ProductCommentDTOConverter;
import com.lemnt.product.application.product.dto.ProductCommentDTO;
import com.lemnt.product.application.product.service.ProductCommentService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 会员评价创建执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductCommentCreateCmdExe {
    private final ProductCommentService productCommentService;

    /**
     * 执行评价写入并返回应用结果。
     */
    public ProductCommentDTO execute(ProductCommentCreateCmd value) {
        return ProductCommentDTOConverter.toDTO(productCommentService.create(value));
    }
}
