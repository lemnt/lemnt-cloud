package com.lemnt.mall.application.product.service;

import com.lemnt.mall.application.product.cmd.ProductSpecCreateCmd;
import com.lemnt.mall.application.product.cmd.ProductSpecUpdateCmd;
import com.lemnt.mall.application.product.cmd.ProductSpecValueCreateCmd;
import com.lemnt.mall.application.product.cmd.ProductSpecValueUpdateCmd;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpecGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpecValueGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSpec;
import com.lemnt.mall.domain.product.model.ProductSpecFilter;
import com.lemnt.mall.domain.product.model.ProductSpecValue;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 规格字典维护，同步当前SKU名称，原订单快照保持不变。 */
@Component
@RequiredArgsConstructor
public class ProductSpecService {
    private final ProductSpecGateway productSpecGateway;
    private final ProductSpecValueGateway productSpecValueGateway;
    private final ProductSkuGateway productSkuGateway;

    /** 创建规格项。 */
    @Transactional(rollbackFor = Exception.class)
    public ProductSpec create(ProductSpecCreateCmd cmd) {
        return save(null, cmd.name(), cmd.remark());
    }

    /** 修改规格项。 */
    @Transactional(rollbackFor = Exception.class)
    public ProductSpec update(ProductSpecUpdateCmd cmd) {
        return save(cmd.id(), cmd.name(), cmd.remark());
    }

    /** 创建规格值。 */
    @Transactional(rollbackFor = Exception.class)
    public ProductSpecValue createValue(ProductSpecValueCreateCmd cmd) {
        return saveValue(null, cmd.specId(), cmd.name(), cmd.remark());
    }

    /** 修改规格值。 */
    @Transactional(rollbackFor = Exception.class)
    public ProductSpecValue updateValue(ProductSpecValueUpdateCmd cmd) {
        return saveValue(cmd.id(), null, cmd.name(), cmd.remark());
    }

    /** 创建或修改规格项，改名同步当前SKU。 */
    private ProductSpec save(Long id, String name, String remark) {
        String normalized = ProductCatalogValidation.text(name, 64, "规格名称");
        if (id != null) {
            ProductCatalogValidation.id(id);
            var old = productSpecGateway.lock(id);
            if (old == null) {
                throw new IllegalArgumentException("规格不存在");
            }
        }
        var result = productSpecGateway.save(new ProductSpec(id, normalized, ProductCatalogValidation.optional(remark, 500)));
        if (id != null) { productSkuGateway.renameProperty(id, normalized); }
        return result;
    }

    /** 创建或修改规格值，规格归属创建后不能更换。 */
    private ProductSpecValue saveValue(Long id, Long specId, String name, String remark) {
        String normalized = ProductCatalogValidation.text(name, 100, "规格值");
        ProductSpecValue old = id == null ? null : productSpecValueGateway.find(id);
        if (id != null && old == null) {
            throw new IllegalArgumentException("规格值不存在");
        }
        Long parentId = old == null ? specId : old.specId();
        ProductCatalogValidation.id(parentId);
        var parent = productSpecGateway.lock(parentId);
        if (parent == null) {
            throw new IllegalArgumentException("规格不存在");
        }
        if (old != null) {
            old = productSpecValueGateway.lock(id);
        }
        var result = productSpecValueGateway.save(new ProductSpecValue(id, parentId, normalized, ProductCatalogValidation.optional(remark, 500)));
        if (id != null) { productSkuGateway.renamePropertyValue(id, normalized); }
        return result;
    }

    /** 规格仍有子值时不可删除，已有SKU保留历史内容。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        ProductCatalogValidation.id(id);
        var old = productSpecGateway.lock(id);
        if (old == null) {
            return false;
        }
        if (productSpecValueGateway.page(new ProductSpecFilter(null, id, 1, 1)).getTotal() > 0) {
            throw new IllegalStateException("规格仍包含规格值");
        }
        return productSpecGateway.delete(id);
    }

    /** 删除规格值不擦除SKU快照。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteValue(Long id) {
        ProductCatalogValidation.id(id);
        var old = productSpecValueGateway.find(id);
        if (old == null) {
            return false;
        }
        var parent = productSpecGateway.lock(old.specId());
        old = productSpecValueGateway.lock(id);
        if (old == null) {
            return false;
        }
        return productSpecValueGateway.delete(id);
    }
}
