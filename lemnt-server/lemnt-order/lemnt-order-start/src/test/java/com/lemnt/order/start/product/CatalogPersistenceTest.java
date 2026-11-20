package com.lemnt.order.start.product;

import com.lemnt.member.client.dto.MemberPointConfigDTO;
import com.lemnt.order.domain.settlement.gateway.SettlementGateway;
import com.lemnt.order.start.checkout.MemorySettlementGateway;
import com.lemnt.order.start.support.CommerceEventPump;
import com.lemnt.order.start.support.CommerceTestDefaults;
import com.lemnt.order.start.support.CommerceTestFixture;
import com.lemnt.order.start.support.MemberLockTestSupport;
import com.lemnt.product.application.product.service.ProductCatalogService;
import com.lemnt.product.application.product.service.ProductInventoryService;
import com.lemnt.product.domain.product.gateway.ProductBrandGateway;
import com.lemnt.product.domain.product.gateway.ProductCategoryGateway;
import com.lemnt.product.domain.product.gateway.ProductSkuGateway;
import com.lemnt.product.domain.product.gateway.ProductSpuGateway;
import com.lemnt.product.domain.product.model.ProductBrand;
import com.lemnt.product.domain.product.model.ProductCategory;
import com.lemnt.product.domain.product.model.ProductCatalogValidation;
import com.lemnt.product.domain.product.model.ProductSku;
import com.lemnt.product.domain.product.model.ProductSpu;

import com.baomidou.lock.LockTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * 活动组合用例的真实四库装配，会员资料及外部渠道使用可控传输。
 */
class CatalogPersistenceTest {
    CommerceTestFixture components;
    CommerceEventPump events;
    ProductCatalogService catalogService;
    ProductInventoryService productInventoryService;
    ProductSkuGateway productSkuGateway;
    ProductSpuGateway productSpuGateway;
    LongSupplier points = () -> 10000L;
    Long categoryId, brandId;

    void setup() throws Exception {
        components = new CommerceTestFixture();
        events = new CommerceEventPump(components);
        events.install();
        CommerceTestDefaults.install(
            components,
            events,
            () -> points.getAsLong(),
            () -> new MemberPointConfigDTO(false, 1, 0, 0),
            () -> null);
        components.register(LockTemplate.class, MemberLockTestSupport.create());
        components.register(
            SettlementGateway.class, new MemorySettlementGateway(new ConcurrentHashMap<>()));
        catalogService = components.get(ProductCatalogService.class);
        productInventoryService = components.get(ProductInventoryService.class);
        productSkuGateway = components.get(ProductSkuGateway.class);
        productSpuGateway = components.get(ProductSpuGateway.class);
        categoryId =
            components
                .get(ProductCategoryGateway.class)
                .saveCategory(new ProductCategory(null, null, "分类", null, 0, "ENABLED"))
                .id();
        brandId =
            components
                .get(ProductBrandGateway.class)
                .saveBrand(new ProductBrand(null, "品牌", null, null, 0, "ENABLED"))
                .id();
    }

    ProductSku draft(String code, int stock) {
        return new ProductSku(
            null,
            null,
            code,
            "sku",
            BigDecimal.TEN,
            stock,
            null,
            Map.of("颜色", code),
            "ON_SHELF");
    }

    ProductSpu create(String code, ProductSku... skus) {
        return catalogService.createSpu(
            ProductCatalogValidation.newSpu(
                new ProductSpu(
                    null,
                    code,
                    "goods",
                    categoryId,
                    brandId,
                    "description",
                    null,
                    null,
                    List.of(skus))));
    }

    long count(String table) {
        return components
            .jdbc(table.startsWith("product_") ? "product" : "promotion")
            .queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }

    void closeSchemaConnection() throws Exception {
        components.close();
    }
}
