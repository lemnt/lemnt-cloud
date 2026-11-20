package com.lemnt.system.infrastructure.config.mapper;

import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.system.domain.config.gateway.ConfigGateway;
import com.lemnt.system.domain.config.model.Config;
import com.lemnt.system.infrastructure.config.gateway.ConfigGatewayImpl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 生产配置DDL、复制SQL和真实Mapper验证容量、唯一键、审计及完整配置读写。
 */
class BusinessConfigPersistenceTest {
    JdbcDataSource source;
    ConfigMapper mapper;
    ConfigGatewayImpl configGateway;
    String schemaDDL;
    final Path sql = Path.of("../../sql");

    @BeforeEach
    void setup() throws Exception {
        source = new JdbcDataSource();
        source.setURL(
            "jdbc:h2:mem:business_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        String schema = Files.readString(sql.resolve("lemnt_system.sql"));
        schema = schema.substring(schema.indexOf("CREATE TABLE `sys_config`"));
        schema = schema.substring(0, schema.indexOf(';'));
        // H2无MySQL字符集排序规则，只移除方言中的COLLATE声明。
        schema =
            schema.replace(" COLLATE utf8mb4_general_ci", "")
                .replace(" COLLATE=utf8mb4_general_ci", "");
        schemaDDL = schema;
        try (var connection = source.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(schema);
        }
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        factory.setMapperLocations(new ClassPathResource("mapper/ConfigMapper.xml"));
        mapper = new SqlSessionTemplate(factory.getObject()).getMapper(ConfigMapper.class);
        configGateway = new ConfigGatewayImpl(mapper, Mockito.mock(RedisStringService.class));
    }

    Config value(String json, String operator) {
        var config = new Config();
        config.setConfigKey("trade.config");
        config.setConfigName("交易配置");
        config.setConfigValue(json);
        config.setUpdateBy(operator);
        return config;
    }

    @Test
    void savesLargeSnapshotAtomicallyAndKeepsCreationAudit() throws Exception {
        String json = "{\"poster\":\"" + "图".repeat(1000) + "\",\"first\":10,\"second\":5}";
        mapper.saveBusinessConfig(value(json, "creator"));
        var first = configGateway.selectByKey("trade.config");
        assertEquals(json, first.getConfigValue());
        assertEquals("creator", first.getCreateBy());
        mapper.saveBusinessConfig(value("{\"first\":20,\"second\":30}", "editor"));
        var second = configGateway.selectByKey("trade.config");
        assertEquals(first.getConfigId(), second.getConfigId());
        assertEquals(first.getCreateTime(), second.getCreateTime());
        assertEquals("creator", second.getCreateBy());
        assertEquals("editor", second.getUpdateBy());
        assertEquals("{\"first\":20,\"second\":30}", second.getConfigValue());
        try (var connection = source.getConnection();
             var statement = connection.createStatement()) {
            assertThrows(
                SQLException.class,
                () ->
                    statement.execute(
                        "INSERT INTO sys_config(config_key) VALUES('trade.config')"));
        }
    }

    @Test
    void concurrentUpsertsKeepOneCompleteSnapshot() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            Callable<Void> writerA =
                () -> {
                    start.await();
                    for (int i = 0; i < 20; i++)
                        mapper.saveBusinessConfig(value("{\"first\":10,\"second\":90}", "a"));
                    return null;
                };
            Callable<Void> writerB =
                () -> {
                    start.await();
                    for (int i = 0; i < 20; i++)
                        mapper.saveBusinessConfig(value("{\"first\":40,\"second\":60}", "b"));
                    return null;
                };
            var a = pool.submit(writerA);
            var b = pool.submit(writerB);
            start.countDown();
            a.get(10, TimeUnit.SECONDS);
            b.get(10, TimeUnit.SECONDS);
            var result =
                JSON.parseObject(configGateway.selectByKey("trade.config").getConfigValue());
            assertEquals(100, result.getIntValue("first") + result.getIntValue("second"));
            try (var connection = source.getConnection();
                 var statement = connection.createStatement();
                 var rows =
                     statement.executeQuery(
                         "SELECT COUNT(*) FROM sys_config WHERE config_key='trade.config'")) {
                assertTrue(rows.next());
                assertEquals(1, rows.getInt(1));
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void failedCacheInvalidationRollsBackSystemWrite() {
        var redis = Mockito.mock(RedisStringService.class);
        Mockito.doThrow(new IllegalStateException("redis failure"))
            .when(redis)
            .delete(ArgumentMatchers.anyString());
        var target = new ConfigGatewayImpl(mapper, redis);
        var factory = new ProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAdvice(
            new TransactionInterceptor(
                new DataSourceTransactionManager(source),
                new AnnotationTransactionAttributeSource()));
        var gateway = (ConfigGateway) factory.getProxy();
        assertThrows(
            IllegalStateException.class,
            () -> gateway.saveBusinessConfig(value("{}", "creator")));
        assertNull(configGateway.selectByKey("trade.config"));
    }

    @Test
    void migrationPreservesFirstMemberRowTradeArraysAndExistingTargetValue() throws Exception {
        try (var connection = source.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA lemnt_system");
            statement.execute("CREATE SCHEMA lemnt_member");
            statement.execute("CREATE SCHEMA lemnt_trade");
            statement.execute(schemaDDL.replace("`sys_config`", "lemnt_system.sys_config"));
            statement.execute(
                "CREATE TABLE lemnt_member.member_config(id INT,point_trade_deduct_enable BOOLEAN,"
                    + "point_trade_deduct_unit_price INT,point_trade_deduct_max_price INT,point_trade_give_point INT,"
                    + "create_by VARCHAR(64),create_time TIMESTAMP,update_by VARCHAR(64),update_time TIMESTAMP)");
            statement.execute(
                "INSERT INTO lemnt_member.member_config VALUES(77,TRUE,5,2,3,'legacy',NULL,NULL,NULL),(88,FALSE,0,0,0,NULL,NULL,NULL,NULL)");
            statement.execute(
                "CREATE TABLE lemnt_trade.trade_config(id INT,after_sale_refund_reasons LONGTEXT,after_sale_return_reasons LONGTEXT,"
                    + "delivery_express_free_enabled BOOLEAN,delivery_express_free_price INT,delivery_pick_up_enabled BOOLEAN,"
                    + "brokerage_enabled BOOLEAN,brokerage_enabled_condition INT,brokerage_bind_mode INT,brokerage_poster_urls LONGTEXT,"
                    + "brokerage_first_percent INT,brokerage_second_percent INT,brokerage_withdraw_min_price INT,brokerage_withdraw_fee_percent INT,"
                    + "brokerage_frozen_days INT,brokerage_withdraw_types LONGTEXT,create_by VARCHAR(64),create_time TIMESTAMP,update_by VARCHAR(64),update_time TIMESTAMP)");
            statement.execute(
                "INSERT INTO lemnt_trade.trade_config VALUES(1,'[\"原因\"]','[]',TRUE,1000,TRUE,TRUE,1,2,'[\"url\"]',10,5,100,2,3,'[1,2]','legacy',NULL,NULL,NULL)");
            ScriptUtils.executeSqlScript(
                connection,
                new FileSystemResource(sql.resolve("upgrade/migrate_business_config.sql")));
            try (var rows =
                     statement.executeQuery(
                         "SELECT config_value,create_by FROM sys_config WHERE config_key='member.point.config'")) {
                assertTrue(rows.next());
                assertEquals(
                    5,
                    JSON.parseObject(rows.getString(1))
                        .getIntValue("pointTradeDeductUnitPrice"));
                assertEquals("legacy", rows.getString(2));
            }
            try (var rows =
                     statement.executeQuery(
                         "SELECT config_value FROM sys_config WHERE config_key='trade.config'")) {
                assertTrue(rows.next());
                var trade = JSON.parseObject(rows.getString(1));
                assertEquals("原因", trade.getJSONArray("afterSaleRefundReasons").getString(0));
                assertTrue(trade.getBooleanValue("deliveryPickUpEnabled"));
                assertEquals(1000, trade.getIntValue("deliveryExpressFreePrice"));
            }
            statement.execute(
                "UPDATE lemnt_member.member_config SET point_trade_deduct_unit_price=99");
            ScriptUtils.executeSqlScript(
                connection,
                new FileSystemResource(sql.resolve("upgrade/migrate_business_config.sql")));
            ScriptUtils.executeSqlScript(
                connection,
                new FileSystemResource(sql.resolve("V20261102__business_config.sql")));
            try (var rows =
                     statement.executeQuery(
                         "SELECT config_value FROM sys_config WHERE config_key='member.point.config'")) {
                assertTrue(rows.next());
                assertEquals(
                    5,
                    JSON.parseObject(rows.getString(1))
                        .getIntValue("pointTradeDeductUnitPrice"));
            }
        }
    }
}
