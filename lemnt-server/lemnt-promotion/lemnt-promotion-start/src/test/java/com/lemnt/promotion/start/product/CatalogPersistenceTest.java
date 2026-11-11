package com.lemnt.promotion.start.product;

import com.lemnt.product.application.product.service.ProductCatalogService;
import com.lemnt.product.application.product.service.ProductInventoryService;
import com.lemnt.product.client.ProductCatalogClient;
import com.lemnt.product.domain.product.model.ProductSku;
import com.lemnt.product.domain.product.model.ProductSpu;
import com.lemnt.promotion.domain.member.gateway.MemberTradeGateway;
import com.lemnt.promotion.domain.member.model.MemberPointConfig;
import com.lemnt.promotion.domain.member.model.MemberTradeChange;
import com.lemnt.promotion.domain.product.gateway.ProductCatalogGateway;
import com.lemnt.promotion.domain.product.gateway.ProductSkuGateway;
import com.lemnt.promotion.domain.product.gateway.ProductSpuGateway;
import com.lemnt.promotion.infrastructure.product.gateway.ProductCatalogGatewayImpl;
import com.lemnt.promotion.infrastructure.product.gateway.ProductSkuGatewayImpl;
import com.lemnt.promotion.infrastructure.product.gateway.ProductSpuGatewayImpl;
import com.lemnt.promotion.start.SqlTestSupport;
import com.lemnt.promotion.start.TableTestComponents;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Connection;
import java.util.UUID;

/**
 * 原组合用例的装配夹具：两套生产SQL、两套事务管理器，商品经真实RPC边界转换。
 */
class CatalogPersistenceTest {
    TableTestComponents components;
    JdbcDataSource source;
    Connection schemaConnection;
    ProductDatabaseFixture catalog;
    ProductCatalogService catalogService;
    ProductInventoryService productInventoryService;
    ProductSkuGateway productSkuGateway;
    ProductSpuGateway productSpuGateway;

    void setup() throws Exception {
        catalog = new ProductDatabaseFixture();
        catalog.setup();
        catalogService = catalog.catalog;
        productInventoryService = catalog.inventory;
        source = new JdbcDataSource();
        source.setURL(
            "jdbc:h2:mem:promotion_"
                + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000");
        schemaConnection = source.getConnection();
        ScriptUtils.executeSqlScript(
            schemaConnection, new ClassPathResource("promotion-sql/lemnt_promotion.sql"));
        components = new TableTestComponents(SqlTestSupport.session("promotion", source), source);
        var client = catalog.client();
        register(ProductSkuGateway.class, new ProductSkuGatewayImpl(), client);
        register(ProductSpuGateway.class, new ProductSpuGatewayImpl(), client);
        register(ProductCatalogGateway.class, new ProductCatalogGatewayImpl(), client);
        components.register(
            MemberTradeGateway.class,
            new MemberTradeGateway() {
                public MemberPointConfig config() {
                    return new MemberPointConfig(false, 1, 0, 0);
                }

                public void apply(MemberTradeChange change) {
                    throw new IllegalStateException("营销测试不能直接变更会员权益");
                }
            });
        productSkuGateway = components.get(ProductSkuGateway.class);
        productSpuGateway = components.get(ProductSpuGateway.class);
    }

    private <T> void register(Class<T> type, T gateway, ProductCatalogClient client) {
        ReflectionTestUtils.setField(gateway, "client", client);
        components.register(type, gateway);
    }

    ProductSku draft(String code, int stock) {
        return catalog.draft(code, stock);
    }

    ProductSpu create(String code, ProductSku... values) {
        return catalog.create(code, values);
    }

    long count(String table) throws Exception {
        try (var connection =
                 (table.startsWith("product_") ? catalog.source : source).getConnection();
             var statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rows.next();
            return rows.getLong(1);
        }
    }

    void closeSchemaConnection() throws Exception {
        if (schemaConnection != null) schemaConnection.close();
        if (catalog != null && catalog.schema != null) catalog.schema.close();
    }
}
