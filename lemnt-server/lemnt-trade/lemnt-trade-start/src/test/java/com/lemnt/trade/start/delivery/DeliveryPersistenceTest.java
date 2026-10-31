package com.lemnt.trade.start.delivery;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.trade.application.config.cmd.TradeConfigUpdateCmd;
import com.lemnt.trade.application.config.service.TradeConfigService;
import com.lemnt.trade.application.delivery.cmd.DeliveryChargeCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryFreeCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryExpressCreateCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryExpressTemplateCreateCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryExpressTemplateUpdateCmd;
import com.lemnt.trade.application.delivery.qry.DeliveryPriceItemQry;
import com.lemnt.trade.application.delivery.qry.DeliveryPriceQry;
import com.lemnt.trade.application.delivery.service.DeliveryManagementService;
import com.lemnt.trade.application.delivery.service.DeliveryPriceService;
import com.lemnt.trade.domain.config.gateway.TradeConfigGateway;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressGateway;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressTemplateGateway;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressFilter;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** 生产 SQL、Mapper/XML、Gateway 及 Spring 事务验证，不使用内存存储替身。 */
class DeliveryPersistenceTest {
    AnnotationConfigApplicationContext context;
    DeliveryManagementService management;
    DeliveryPriceService pricing;
    TradeConfigGateway config;

    @BeforeEach
    void setup() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        management = context.getBean(DeliveryManagementService.class);
        pricing = context.getBean(DeliveryPriceService.class);
        config = context.getBean(TradeConfigGateway.class);
    }

    @AfterEach
    void close() {
        context.close();
    }

    @Test
    void configurationAndCompanyRoundTripWithLogicalDeletion() {
        assertFalse(config.get().deliveryExpressFreeEnabled());
        assertEquals(List.of(), config.get().afterSaleRefundReasons());
        Long id = management.createExpress(new DeliveryExpressCreateCmd("SF", "顺丰", null, 1, 0));
        var gateway = context.getBean(DeliveryExpressGateway.class);
        assertEquals("顺丰", gateway.byCode("SF").name());
        assertEquals(1, gateway.page(new DeliveryExpressFilter("顺", 0, 1, 20)).getTotal());
        assertThrows(ServiceException.class, () -> management.createExpress(
                new DeliveryExpressCreateCmd("SF", "重复公司", null, 1, 0)));
        assertTrue(management.deleteExpress(id));
        assertNull(gateway.find(id));
        assertNotNull(management.createExpress(new DeliveryExpressCreateCmd("SF", "新公司", null, 1, 0)));
    }

    @Test
    void countFreightRoundsUpAndDistributesEveryFen() {
        var id = template(1, "2", 501, "2", 200, List.of());
        var quote = pricing.calculate(new DeliveryPriceQry(110101, false, List.of(
                item(1L, id, 2, null, null, 1000), item(2L, id, 3, null, null, 2000))));
        assertEquals(901, quote.deliveryPrice());
        assertEquals(360, quote.itemPrices().get(1L));
        assertEquals(541, quote.itemPrices().get(2L));
        assertEquals(quote.deliveryPrice(), quote.itemPrices().values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void weightAndVolumeUseQuantityAndCeiling() {
        var weight = template(2, "1", 600, "0.5", 100, List.of());
        var volume = template(3, "0.1", 300, "0.1", 50, List.of());
        var result = pricing.calculate(new DeliveryPriceQry(110101, false, List.of(
                item(1L, weight, 2, "0.76", null, 1000), item(2L, volume, 3, null, "0.11", 2000))));
        assertEquals(800, result.itemPrices().get(1L));
        assertEquals(450, result.itemPrices().get(2L));
        assertEquals(1250, result.deliveryPrice());
    }

    @Test
    void regionalFreeRequiresBothAmountAndQuantityAndGlobalFreeIsPrioritized() {
        var id = template(1, "1", 600, "1", 100,
                List.of(new DeliveryFreeCmd(null, List.of(110101), 2000, 2)));
        assertEquals(0, price(id, 2, 2000));
        assertEquals(700, price(id, 2, 1999));
        assertEquals(600, price(id, 1, 2000));
        assertEquals(0, pricing.calculate(new DeliveryPriceQry(110101, true,
                List.of(item(1L, id, 1, null, null, 100)))).deliveryPrice());
        saveConfig(true, 1500);
        assertEquals(0, price(id, 1, 1500));
        assertEquals(600, price(id, 1, 1499));
        assertEquals(List.of("不想要了"), config.get().afterSaleRefundReasons());
    }

    @Test
    void missingAreaOrWeightAndDuplicateSkuCannotSilentlyBecomeFree() {
        var id = template(2, "1", 100, "1", 100, List.of());
        assertThrows(ServiceException.class, () -> pricing.calculate(new DeliveryPriceQry(110102, false,
                List.of(item(1L, id, 1, "1", null, 1000)))));
        assertThrows(ServiceException.class, () -> pricing.calculate(new DeliveryPriceQry(110101, false,
                List.of(item(1L, id, 1, null, null, 1000)))));
        assertThrows(ServiceException.class, () -> pricing.calculate(new DeliveryPriceQry(110101, false,
                List.of(item(1L, id, 1, "1", null, 1000), item(1L, id, 1, "1", null, 1000)))));
    }

    @Test
    void templateUpdateRetainsRuleIdentityAndRejectsForeignRuleWithoutPartialWrites() {
        var id = template(1, "1", 100, "1", 100, List.of());
        var otherId = template(1, "2", 200, "2", 200, List.of());
        var old = management.detail(id).charges().get(0);
        assertTrue(management.updateTemplate(new DeliveryExpressTemplateUpdateCmd(id, "更新模板", 2, 5,
                List.of(new DeliveryChargeCmd(old.id(), List.of(110101), bd("2"), 300, bd("1"), 50)),
                List.of(new DeliveryFreeCmd(null, List.of(110101), 1000, 5)))));
        var updated = management.detail(id);
        assertEquals(old.id(), updated.charges().get(0).id());
        assertEquals(2, updated.charges().get(0).chargeMode());
        var foreign = management.detail(otherId).charges().get(0);
        assertThrows(ServiceException.class, () -> management.updateTemplate(new DeliveryExpressTemplateUpdateCmd(
                id, "不应保存", 1, 0, List.of(
                        new DeliveryChargeCmd(null, List.of(110103), bd("1"), 999, bd("1"), 999),
                        new DeliveryChargeCmd(foreign.id(), List.of(110104), bd("1"), 999, bd("1"), 999)), List.of())));
        assertEquals("更新模板", management.detail(id).name());
        assertEquals(1, management.detail(id).charges().size());
        assertEquals(1, management.detail(id).frees().size());
    }

    @Test
    void ruleAreaOverlapIsRejectedAndDeleteRemovesAllActiveRows() {
        assertThrows(ServiceException.class, () -> management.createTemplate(new DeliveryExpressTemplateCreateCmd(
                "非法地区", 1, 0, List.of(
                        new DeliveryChargeCmd(null, List.of(110101), bd("1"), 1, bd("1"), 1),
                        new DeliveryChargeCmd(null, List.of(110101), bd("1"), 1, bd("1"), 1)), List.of())));
        var id = template(1, "1", 100, "1", 100,
                List.of(new DeliveryFreeCmd(null, List.of(110101), 1000, 5)));
        assertTrue(management.deleteTemplate(id));
        assertNull(management.detail(id));
        assertThrows(ServiceException.class, () -> price(id, 1, 100));
    }

    @Test
    void concurrentCreatesCannotPersistDuplicateTemplateNames() throws Exception {
        var pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        var start = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.Callable<Boolean> create = () -> {
            start.await();
            try {
                management.createTemplate(new DeliveryExpressTemplateCreateCmd("并发模板", 1, 0,
                        List.of(new DeliveryChargeCmd(null, List.of(110101), bd("1"), 100, bd("1"), 50)),
                        List.of()));
                return true;
            } catch (ServiceException duplicate) {
                return false;
            }
        };
        try {
            var first = pool.submit(create);
            var second = pool.submit(create);
            start.countDown();
            assertNotEquals(first.get(), second.get());
            assertEquals(1, context.getBean(DeliveryExpressTemplateGateway.class).list("并发模板").size());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void permissionSeedIsRepeatableAndNeverGrantsRoles() throws Exception {
        try (var connection = context.getBean(JdbcDataSource.class).getConnection();
                var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE sys_menu(menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,menu_name"
                    + " VARCHAR(100),parent_id BIGINT,order_num INT,path VARCHAR(200),component VARCHAR(255),"
                    + "is_frame INT,is_cache INT,menu_type CHAR(1),visible CHAR(1),status CHAR(1),perms"
                    + " VARCHAR(100),icon VARCHAR(100),create_by VARCHAR(64),create_time TIMESTAMP)");
            statement.execute("CREATE TABLE sys_role_menu(role_id BIGINT,menu_id BIGINT)");
            var script = new ClassPathResource("setup-sql/V20261028__trade_delivery_permissions.sql");
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, script);
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_menu WHERE menu_type='F'")) {
                rows.next();
                assertEquals(12, rows.getInt(1));
            }
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_role_menu")) {
                rows.next();
                assertEquals(0, rows.getInt(1));
            }
        }
    }

    private Long template(int mode, String start, int price, String extra, int extraPrice, List<DeliveryFreeCmd> frees) {
        return management.createTemplate(new DeliveryExpressTemplateCreateCmd(UUID.randomUUID().toString(), mode, 0,
                List.of(new DeliveryChargeCmd(null, List.of(110101), bd(start), price, bd(extra), extraPrice)), frees));
    }

    private int price(Long id, int count, int paid) {
        return pricing.calculate(new DeliveryPriceQry(110101, false,
                List.of(item(1L, id, count, null, null, paid)))).deliveryPrice();
    }

    private void saveConfig(boolean free, int threshold) {
        context.getBean(TradeConfigService.class).update(new TradeConfigUpdateCmd(List.of("不想要了"), List.of("质量问题"),
                free, threshold, false, false, 1, 1, List.of(), 0, 0, 0, 0, 0, List.of()));
    }

    private DeliveryPriceItemQry item(Long sku, Long template, int count, String weight, String volume, int paid) {
        return new DeliveryPriceItemQry(sku, template, count, bd(weight), bd(volume), paid);
    }

    private BigDecimal bd(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @ComponentScan(basePackages = {"com.lemnt.trade.infrastructure.delivery.gateway",
            "com.lemnt.trade.infrastructure.config.gateway", "com.lemnt.trade.application.delivery.service",
            "com.lemnt.trade.application.config.service"})
    @MapperScan(basePackages = {"com.lemnt.trade.infrastructure.delivery.mapper",
            "com.lemnt.trade.infrastructure.config.mapper"})
    static class TestConfig {
        @Bean
        JdbcDataSource dataSource() {
            var source = new JdbcDataSource();
            source.setURL("jdbc:h2:mem:delivery_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
            return source;
        }

        @Bean(destroyMethod = "close")
        Connection schema(JdbcDataSource source) throws Exception {
            var connection = source.getConnection();
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("trade-sql/V7__delivery_config.sql"));
            return connection;
        }

        @Bean
        SqlSessionFactory sqlSessionFactory(JdbcDataSource source, Connection schema) throws Exception {
            var config = new MybatisConfiguration();
            config.setMapUnderscoreToCamelCase(true);
            var interceptor = new MybatisPlusInterceptor();
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
            config.addInterceptor(interceptor);
            var factory = new MybatisSqlSessionFactoryBean();
            factory.setDataSource(source);
            factory.setConfiguration(config);
            factory.setGlobalConfig(new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
            factory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml"));
            return factory.getObject();
        }

        @Bean
        PlatformTransactionManager transactionManager(JdbcDataSource source) {
            return new DataSourceTransactionManager(source);
        }
    }
}
