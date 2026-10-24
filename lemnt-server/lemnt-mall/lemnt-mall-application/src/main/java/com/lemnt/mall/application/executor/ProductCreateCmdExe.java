package com.lemnt.mall.application.executor;
import com.lemnt.mall.application.cmd.ProductCreateCmd;
import com.lemnt.mall.domain.gateway.ProductGateway;
import com.lemnt.mall.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component @RequiredArgsConstructor public class ProductCreateCmdExe {
 private final ProductGateway gateway;
 @Transactional(rollbackFor=Exception.class) public Product execute(ProductCreateCmd cmd){if(cmd==null)throw new IllegalArgumentException("商品参数不能为空");if(gateway.findByCode(cmd.productCode())!=null)throw new IllegalArgumentException("商品编码已存在");return gateway.save(Product.create(cmd.productCode(),cmd.name(),cmd.description(),cmd.mainImage(),cmd.price(),cmd.stock()));}
}