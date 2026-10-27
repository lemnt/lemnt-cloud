package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.ProductCreateCmd;
import com.lemnt.mall.domain.product.gateway.ProductGateway;
import com.lemnt.mall.domain.product.model.Product;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 商品创建命令执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductCreateCmdExe {
    private final ProductGateway productGateway;

    /**
     * 执行商品创建命令。
     */
    @Transactional(rollbackFor=Exception.class)
    public Product execute(ProductCreateCmd cmd) {
        if(cmd==null) {
            throw new IllegalArgumentException("商品参数不能为空");
        }
        if(productGateway.findByCode(cmd.productCode())!=null) {
            throw new IllegalArgumentException("商品编码已存在");
        }
        return productGateway.save(Product.create(cmd.productCode(),
                cmd.name(),
                cmd.description(),
                cmd.mainImage(),
                cmd.price(),
                cmd.stock()));
    }
}
