package com.lemnt.mall.application.product.service;

import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.gateway.StockReservationGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSpu;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/** 商品回收和SKU增删编排，保留订单、预占和评价使用的历史编号。 */
@Component
@RequiredArgsConstructor
public class ProductManagementService {
    private final ProductSpuGateway productSpuGateway;
    private final ProductSkuGateway productSkuGateway;
    private final StockReservationGateway stockReservationGateway;
    private final ProductCatalogService catalogService;

    /** 按请求顺序返回批量详情，最多100个SPU，各表只批量读取一次。 */
    @Transactional(readOnly = true)
    public List<ProductSpu> batchDetails(List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > 100 || ids.stream().anyMatch(id -> id == null || id <= 0)
        ) {
            throw new IllegalArgumentException("商品编号须为1至100个正整数");
        }
        var requested = ids.stream().distinct().toList();
        var parents = productSpuGateway.findSpus(requested).stream()
                .collect(Collectors.toMap(ProductSpu::id, value -> value));
        var children = productSkuGateway.skusBySpuIds(requested);
        var grouped = children.stream().collect(Collectors.groupingBy(ProductSku::spuId));
        return requested.stream().filter(parents::containsKey).map(id -> {
            var value = parents.get(id);
            return new ProductSpu(value.id(), value.code(), value.name(), value.categoryId(), value.brandId(),
                    value.description(), value.image(), value.status(), grouped.getOrDefault(id, List.of()), value.deliveryTypes(), value.deliveryTemplateId(), value.keyword(), value.introduction(), value.sliderPicUrls(), value.sort());
        }).toList();
    }

    private ProductSpu lock(Long id) {
        ProductCatalogValidation.id(id);
        var value = productSpuGateway.lockSpu(id);
        if (value == null
        ) {
            throw new IllegalArgumentException("商品不存在");
        }
        return value;
    }

    /** 商品只能从下架状态进入回收站。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean recycle(Long id) {
        var value = lock(id);
        if ("RECYCLED".equals(value.status())
        ) {
            return true;
        }
        if (!"OFF_SHELF".equals(value.status())
        ) {
            throw new IllegalStateException("请先下架商品再回收");
        }
        return productSpuGateway.transition(id, "OFF_SHELF", "RECYCLED");
    }

    /** 恢复商品为下架状态，须经上架校验后才能出售。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean restore(Long id) {
        var value = lock(id);
        if ("OFF_SHELF".equals(value.status())
        ) {
            return true;
        }
        if (!"RECYCLED".equals(value.status())
        ) {
            throw new IllegalStateException("商品不在回收站");
        }
        return productSpuGateway.transition(id, "RECYCLED", "OFF_SHELF");
    }

    /** 删除回收站商品及其SKU，保留历史行且不能再次恢复。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        var value = lock(id);
        if ("DELETED".equals(value.status())
        ) {
            return true;
        }
        if (!"RECYCLED".equals(value.status())
        ) {
            throw new IllegalStateException("只能删除回收站商品");
        }
        for (var sku : productSkuGateway.catalogSkus(id)
        ) {
            productSkuGateway.lockById(sku.id());
            if (stockReservationGateway.hasActive(sku.id())
            ) {
                throw new IllegalStateException("商品仍有库存预占，不能删除");
            }
            productSkuGateway.deleteSku(sku.id());
        }
        return productSpuGateway.transition(id, "RECYCLED", "DELETED");
    }

}
