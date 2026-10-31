package com.lemnt.trade.start.payment;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.trade.domain.payment.gateway.PayAppGateway;
import com.lemnt.trade.domain.payment.gateway.PayChannelConfigGateway;
import com.lemnt.trade.domain.payment.model.PayApp;
import com.lemnt.trade.domain.payment.model.PayChannelConfig;

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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** 生产 SQL、DO、Mapper、Gateway 验证支付应用与渠道配置，不使用内存替身。 */
class PayConfigPersistenceTest {
    AnnotationConfigApplicationContext context;
    PayAppGateway apps;
    PayChannelConfigGateway channels;

    @BeforeEach
    void setup() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        apps = context.getBean(PayAppGateway.class);
        channels = context.getBean(PayChannelConfigGateway.class);
    }

    @AfterEach
    void close() {
        context.close();
    }

    private PayApp app(String appKey) {
        return new PayApp(null, appKey, "商城", 0, "备注", "https://example.test/pay",
                "https://example.test/refund", null);
    }

    private PayChannelConfig channel(Long appId, String code, String config) {
        return new PayChannelConfig(null, appId, code, 0, new BigDecimal("1.5"), "备注", config, null);
    }

    @Test
    void appKeyIsUniqueAndLogicalDeleteHidesRow() {
        var created = apps.insert(app("app_key"));
        assertNotNull(created.id());
        assertEquals("app_key", apps.findByAppKey("app_key").appKey());
        assertTrue(apps.existsAppKey("app_key", null));
        assertFalse(apps.existsAppKey("app_key", created.id()));
        assertTrue(apps.delete(created.id()));
        assertNull(apps.find(created.id()));
        assertFalse(apps.existsAppKey("app_key", null));
    }

    @Test
    void channelConfigRoundTripsAndIsScopedToApp() {
        var app = apps.insert(app("app_key"));
        var created = channels.insert(channel(app.id(), "ALIPAY", "{\"merchant\":\"m\"}"));
        assertNotNull(created.id());
        assertEquals(new BigDecimal("1.50"), channels.find(created.id()).feeRate());
        assertEquals("{\"merchant\":\"m\"}", channels.find(created.id()).config());
        assertTrue(channels.exists(app.id(), "ALIPAY", null));
        assertEquals(1, channels.listByAppId(app.id()).size());
        assertTrue(channels.channelReferenced(app.id()));
        assertTrue(channels.delete(created.id()));
        assertFalse(channels.channelReferenced(app.id()));
    }

    @Test
    void updateKeepsAppKeyAndPersistsStatus() {
        var created = apps.insert(app("app_key"));
        var updated = new PayApp(created.id(), created.appKey(), "新名字", 1, null, null, null, null);
        assertTrue(apps.update(updated));
        var value = apps.find(created.id());
        assertEquals("app_key", value.appKey());
        assertEquals("新名字", value.name());
        assertEquals(1, value.status());
        assertNull(value.orderNotifyUrl());
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @ComponentScan(basePackages = {"com.lemnt.trade.infrastructure.payment.gateway"})
    @MapperScan(basePackages = {"com.lemnt.trade.infrastructure.payment.mapper"})
    static class TestConfig {
        @Bean
        JdbcDataSource dataSource() {
            var source = new JdbcDataSource();
            source.setURL("jdbc:h2:mem:pay_config_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
            return source;
        }

        @Bean(destroyMethod = "close")
        Connection schema(JdbcDataSource source) throws Exception {
            var connection = source.getConnection();
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("trade-sql/V10__pay_app_channel.sql"));
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
