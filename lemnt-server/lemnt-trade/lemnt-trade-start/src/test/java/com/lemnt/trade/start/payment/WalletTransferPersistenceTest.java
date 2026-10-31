package com.lemnt.trade.start.payment;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.trade.domain.payment.gateway.WalletGateway;
import com.lemnt.trade.domain.payment.gateway.WalletTransferGateway;
import com.lemnt.trade.domain.payment.model.WalletTransfer;
import com.lemnt.trade.domain.payment.model.WalletTransferFilter;

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

/** 生产 SQL、DO、Mapper、Gateway 验证钱包转账。 */
class WalletTransferPersistenceTest {
    AnnotationConfigApplicationContext context;
    WalletGateway wallets;
    WalletTransferGateway transfers;

    @BeforeEach
    void setup() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        wallets = context.getBean(WalletGateway.class);
        transfers = context.getBean(WalletTransferGateway.class);
    }

    @AfterEach
    void close() {
        context.close();
    }

    @Test
    void requestIsIdempotentAndVisibleFromBothMembers() {
        var from = wallets.createIfAbsent(1L);
        var to = wallets.createIfAbsent(2L);
        var value = new WalletTransfer(null, "TF1", "REQ1", from.id(), 1L, to.id(), 2L,
                new BigDecimal("10.00"), "备注", "SUCCESS", null);
        var created = transfers.createIfAbsent(value);
        var again = transfers.createIfAbsent(value);
        assertEquals(created.id(), again.id());
        assertNotNull(transfers.findByNo("TF1"));
        assertEquals(1, transfers.page(new WalletTransferFilter(1L, null, null, null, 1, 20)).getRecords().size());
        assertEquals(1, transfers.page(new WalletTransferFilter(2L, null, null, null, 1, 20)).getRecords().size());
        assertEquals(1, transfers.page(new WalletTransferFilter(null, 1L, null, null, 1, 20)).getRecords().size());
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @ComponentScan(basePackages = {"com.lemnt.trade.infrastructure.payment.gateway"})
    @MapperScan(basePackages = {"com.lemnt.trade.infrastructure.payment.mapper"})
    static class TestConfig {
        @Bean
        JdbcDataSource dataSource() {
            var source = new JdbcDataSource();
            source.setURL("jdbc:h2:mem:transfer_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
            return source;
        }

        @Bean(destroyMethod = "close")
        Connection schema(JdbcDataSource source) throws Exception {
            var connection = source.getConnection();
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("trade-sql/V12__member_wallet.sql"));
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("trade-sql/V14__wallet_transfer.sql"));
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
