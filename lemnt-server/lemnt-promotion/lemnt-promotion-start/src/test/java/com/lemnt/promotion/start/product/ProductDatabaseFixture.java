package com.lemnt.promotion.start.product;

import com.lemnt.product.application.dubbo.ProductCatalogClientImpl;
import com.lemnt.product.application.product.service.ProductCatalogService;
import com.lemnt.product.application.product.service.ProductInventoryService;
import com.lemnt.product.client.ProductCatalogClient;
import com.lemnt.product.domain.product.gateway.*;
import com.lemnt.product.domain.product.model.*;
import com.lemnt.promotion.start.SqlTestSupport;
import com.lemnt.promotion.start.TableTestComponents;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;

/**
 * 真实商品库及生产Provider，营销端只通过Client调用，不共享数据库。
 */
class ProductDatabaseFixture {
    JdbcDataSource source;
    Connection schema;
    TableTestComponents components;
    ProductCatalogService catalog;
    ProductInventoryService inventory;
    ProductSkuGateway skus;
    ProductSpuGateway spus;
    Long categoryId;
    Long brandId;

    void setup() throws Exception {
        source = new JdbcDataSource();
        source.setURL(
            "jdbc:h2:mem:product_remote_"
                + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000");
        schema = source.getConnection();
        ScriptUtils.executeSqlScript(
            schema, new ClassPathResource("product-sql/lemnt_product.sql"));
        components = new TableTestComponents(SqlTestSupport.session("product", source), source);
        catalog = components.get(ProductCatalogService.class);
        inventory = components.get(ProductInventoryService.class);
        skus = components.get(ProductSkuGateway.class);
        spus = components.get(ProductSpuGateway.class);
        categoryId =
            components
                .get(ProductCategoryGateway.class)
                .saveCategory(new ProductCategory(null, null, "category", null, 0, "ENABLED"))
                .id();
        brandId =
            components
                .get(ProductBrandGateway.class)
                .saveBrand(new ProductBrand(null, "brand", null, null, 0, "ENABLED"))
                .id();
    }

    ProductCatalogClient client() {
        return new ProductCatalogClientImpl(
            components.get(ProductCatalogGateway.class), inventory, skus, spus);
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

    ProductSpu create(String code, ProductSku... values) {
        return catalog.createSpu(
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
                    List.of(values))));
    }

    void shelf(Long id, boolean on) {
        spus.shelf(id, on);
    }
}
