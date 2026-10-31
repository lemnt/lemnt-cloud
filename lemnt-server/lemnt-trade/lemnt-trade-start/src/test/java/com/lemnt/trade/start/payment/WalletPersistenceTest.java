package com.lemnt.trade.start.payment;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.trade.domain.payment.gateway.WalletGateway;
import com.lemnt.trade.domain.payment.model.Wallet;
import com.lemnt.trade.domain.payment.model.WalletFilter;
import com.lemnt.trade.domain.payment.model.WalletTransaction;
import com.lemnt.trade.domain.payment.model.WalletTransactionFilter;

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

/** 生产 SQL、DO、Mapper、Gateway 验证钱包唯一账户、行锁查询与流水。 */
class WalletPersistenceTest {
    AnnotationConfigApplicationContext context;
    WalletGateway wallets;

    @BeforeEach
    void setup() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        wallets = context.getBean(WalletGateway.class);
    }

    @AfterEach
    void close() {
        context.close();
    }

    @Test
    void accountIsUniquePerMember() {
        var first = wallets.createIfAbsent(1L);
        var second = wallets.createIfAbsent(1L);
        assertEquals(first.id(), second.id());
        assertNotNull(wallets.findByMemberId(1L));
        assertNotNull(wallets.lockByMemberId(1L));
        assertEquals(1, wallets.page(new WalletFilter(1L, 1, 20)).getRecords().size());
    }

    @Test
    void updateAndTransactionPagePersist() {
        var created = wallets.createIfAbsent(2L);
        var updated = new Wallet(created.id(), 2L, new BigDecimal("12.00"), new BigDecimal("3.00"),
                new BigDecimal("5.00"), new BigDecimal("10.00"), created.createTime());
        assertTrue(wallets.update(updated));
        var stored = wallets.findByMemberId(2L);
        assertEquals(new BigDecimal("12.00"), stored.balance());
        assertEquals(new BigDecimal("3.00"), stored.freezePrice());

        wallets.insertTransaction(new WalletTransaction(null, "WT1", created.id(), "RECHARGE", "R1", "充值",
                new BigDecimal("10.00"), new BigDecimal("12.00"), null));
        var found = wallets.findTransaction(created.id(), "RECHARGE", "R1");
        assertNotNull(found);
        assertEquals(new BigDecimal("10.00"), found.amount());
        assertEquals(1, wallets.transactionPage(new WalletTransactionFilter(created.id(), null, 1, 20))
                .getRecords().size());
        assertEquals(0, wallets.transactionPage(new WalletTransactionFilter(created.id(), "PAYMENT", 1, 20))
                .getRecords().size());
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @ComponentScan(basePackages = {"com.lemnt.trade.infrastructure.payment.gateway"})
    @MapperScan(basePackages = {"com.lemnt.trade.infrastructure.payment.mapper"})
    static class TestConfig {
        @Bean
        JdbcDataSource dataSource() {
            var source = new JdbcDataSource();
            source.setURL("jdbc:h2:mem:wallet_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
            return source;
        }

        @Bean(destroyMethod = "close")
        Connection schema(JdbcDataSource source) throws Exception {
            var connection = source.getConnection();
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("trade-sql/V12__member_wallet.sql"));
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
