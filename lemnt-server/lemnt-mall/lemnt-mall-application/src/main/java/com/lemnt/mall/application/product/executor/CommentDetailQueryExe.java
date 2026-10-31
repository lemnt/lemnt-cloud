package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.converter.CommentDTOConverter;
import com.lemnt.mall.application.product.dto.ProductCommentDTO;
import com.lemnt.mall.domain.product.gateway.ProductCommentGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 商品评价详情查询。 */
@Component
@RequiredArgsConstructor
public class CommentDetailQueryExe {
    private final ProductCommentGateway productCommentGateway;

    /** 后台可查询隐藏评价，权限由管理接口控制。 */
    public ProductCommentDTO execute(Long id) {
        ProductCatalogValidation.id(id);
        return CommentDTOConverter.toDTO(productCommentGateway.find(id));
    }
}
