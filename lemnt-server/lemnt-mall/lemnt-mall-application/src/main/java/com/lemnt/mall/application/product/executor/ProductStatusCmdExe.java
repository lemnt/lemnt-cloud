package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.domain.product.gateway.ProductGateway;
import com.lemnt.mall.domain.product.model.Product;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 商品状态命令执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductStatusCmdExe {
    private final ProductGateway productGateway;

    /**
     * 执行商品状态命令。
     */
    @Transactional(rollbackFor=Exception.class)
    public boolean execute(Long id, boolean onShelf) {
        Product p=productGateway.findById(id);
        if(p==null) {
            throw new IllegalArgumentException("商品不存在");
        }
        String status=onShelf?"ON_SHELF":"OFF_SHELF";
        return productGateway.updateStatus(id, status);
    }
}
