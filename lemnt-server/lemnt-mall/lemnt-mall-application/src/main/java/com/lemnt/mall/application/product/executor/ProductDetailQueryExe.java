package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.domain.product.gateway.ProductGateway;
import com.lemnt.mall.domain.product.model.Product;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 商品详情查询执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductDetailQueryExe {
    private final ProductGateway productGateway;

    /**
     * 执行商品详情查询。
     */
    public Product execute(Long id) {
        if(id==null||id<=0) {
            throw new IllegalArgumentException("商品ID不合法");
        }
        return productGateway.findById(id);
    }
}
