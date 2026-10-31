package com.lemnt.trade.start.payment;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.trade.domain.payment.gateway.RechargePackageGateway;
import com.lemnt.trade.domain.payment.gateway.WalletGateway;
import com.lemnt.trade.domain.payment.gateway.WalletRechargeGateway;
import com.lemnt.trade.domain.payment.model.RechargePackage;
import com.lemnt.trade.domain.payment.model.WalletRecharge;

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

/** 生产 SQL、DO、Mapper、Gateway 验证充值套餐与充值单。 */
class RechargePersistenceTest {
    AnnotationConfigApplicationContext context;
    WalletGateway wallets;
    RechargePackageGateway packages;
    WalletRechargeGateway recharges;

    @BeforeEach
    void setup() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        wallets = context.getBean(WalletGateway.class);
        packages = context.getBean(RechargePackageGateway.class);
        recharges = context.getBean(WalletRechargeGateway.class);
    }

    @AfterEach
    void close() {
        context.close();
    }

    private RechargePackage pkg(String name, int status) {
        return new RechargePackage(null, name, new BigDecimal("100.00"), new BigDecimal("20.00"), status, null);
    }

    private WalletRecharge recharge(Long walletId, String requestNo) {
        return new WalletRecharge(null, "RC" + requestNo, requestNo, walletId, 1L, 1L, new BigDecimal("100.00"),
                new BigDecimal("20.00"), new BigDecimal("120.00"), "PENDING", null, null, null, null);
    }

    @Test
    void packageNameUniqueAndEnabledListFiltersStatus() {
        var enabled = packages.insert(pkg("标准", 0));
        packages.insert(pkg("停用", 1));
        assertTrue(packages.existsName("标准", null));
        assertFalse(packages.existsName("标准", enabled.id()));
        assertEquals(1, packages.listEnabled().size());
        var updated = new RechargePackage(enabled.id(), "标准2", new BigDecimal("50.00"), BigDecimal.ZERO, 0,
                enabled.createTime());
        assertTrue(packages.update(updated));
        assertEquals(new BigDecimal("50.00"), packages.find(enabled.id()).payPrice());
    }

    @Test
    void rechargeRequestUniqueAndStatusUpdate() {
        var wallet = wallets.createIfAbsent(1L);
        var created = recharges.createIfAbsent(recharge(wallet.id(), "REQ1"));
        var again = recharges.createIfAbsent(recharge(wallet.id(), "REQ1"));
        assertEquals(created.id(), again.id());
        assertNotNull(recharges.lockByNo(created.rechargeNo()));
        assertTrue(recharges.packageReferenced(1L));
        var paid = new WalletRecharge(created.id(), created.rechargeNo(), created.requestNo(), created.walletId(),
                created.memberId(), created.packageId(), created.payPrice(), created.bonusPrice(),
                created.totalPrice(), "PAID", "ALIPAY", null, null, created.createTime());
        assertTrue(recharges.update(paid));
        assertEquals("PAID", recharges.findByNo(created.rechargeNo()).status());
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @ComponentScan(basePackages = {"com.lemnt.trade.infrastructure.payment.gateway"})
    @MapperScan(basePackages = {"com.lemnt.trade.infrastructure.payment.mapper"})
    static class TestConfig {
        @Bean
        JdbcDataSource dataSource() {
            var source = new JdbcDataSource();
            source.setURL("jdbc:h2:mem:recharge_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
            return source;
        }

        @Bean(destroyMethod = "close")
        Connection schema(JdbcDataSource source) throws Exception {
            var connection = source.getConnection();
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("trade-sql/V12__member_wallet.sql"));
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("trade-sql/V13__wallet_recharge.sql"));
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
