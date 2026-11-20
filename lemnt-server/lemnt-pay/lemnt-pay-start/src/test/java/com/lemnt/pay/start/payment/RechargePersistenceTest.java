package com.lemnt.pay.start.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.pay.application.payment.dto.PaymentTrendDayDTO;
import com.lemnt.pay.application.payment.executor.PaymentTrendQueryExe;
import com.lemnt.pay.application.payment.qry.PaymentTrendQry;
import com.lemnt.pay.domain.payment.gateway.PaymentTrendGateway;
import com.lemnt.pay.domain.payment.gateway.RechargePackageGateway;
import com.lemnt.pay.domain.payment.gateway.WalletGateway;
import com.lemnt.pay.domain.payment.gateway.WalletRechargeGateway;
import com.lemnt.pay.domain.payment.model.RechargePackage;
import com.lemnt.pay.domain.payment.model.WalletRecharge;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 生产 SQL、DO、Mapper、Gateway 验证充值套餐与充值单。
 */
class RechargePersistenceTest {
    AnnotationConfigApplicationContext context;
    WalletGateway wallets;
    RechargePackageGateway packages;
    WalletRechargeGateway recharges;

    @BeforeEach
    void setup() {
        context =
                new AnnotationConfigApplicationContext(
                        TestConfig.class, PaymentPersistenceDependencies.class);
        wallets = context.getBean(WalletGateway.class);
        packages = context.getBean(RechargePackageGateway.class);
        recharges = context.getBean(WalletRechargeGateway.class);
    }

    @AfterEach
    void close() {
        context.close();
    }

    private RechargePackage pkg(String name, int status) {
        return new RechargePackage(
                null, name, new BigDecimal("100.00"), new BigDecimal("20.00"), status, null);
    }

    private WalletRecharge recharge(Long walletId, String requestNo) {
        return new WalletRecharge(
                null,
                "RC" + requestNo,
                requestNo,
                walletId,
                1L,
                1L,
                new BigDecimal("100.00"),
                new BigDecimal("20.00"),
                new BigDecimal("120.00"),
                "PENDING",
                null,
                null,
                null,
                null);
    }

    @Test
    void packageNameUniqueAndEnabledListFiltersStatus() {
        var enabled = packages.insert(pkg("标准", 0));
        packages.insert(pkg("停用", 1));
        assertTrue(packages.existsName("标准", null));
        assertFalse(packages.existsName("标准", enabled.id()));
        assertEquals(1, packages.listEnabled().size());
        var updated =
                new RechargePackage(
                        enabled.id(),
                        "标准2",
                        new BigDecimal("50.00"),
                        BigDecimal.ZERO,
                        0,
                        enabled.createTime());
        assertTrue(packages.update(updated));
        assertEquals(new BigDecimal("50.00"), packages.find(enabled.id()).payPrice());
    }

    @Test
    void rechargeRequestUniqueAndStatusUpdate() {
        var wallet = wallets.createIfAbsent(1L);
        var created = recharges.createIfAbsent(recharge(wallet.id(), "REQ1"));
        assertEquals(0, BigDecimal.ZERO.compareTo(recharges.paidRechargeTotal()));
        var again = recharges.createIfAbsent(recharge(wallet.id(), "REQ1"));
        assertEquals(created.id(), again.id());
        assertNotNull(recharges.lockByNo(created.rechargeNo()));
        assertTrue(recharges.packageReferenced(1L));
        var paid =
                new WalletRecharge(
                        created.id(),
                        created.rechargeNo(),
                        created.requestNo(),
                        created.walletId(),
                        created.memberId(),
                        created.packageId(),
                        created.payPrice(),
                        created.bonusPrice(),
                        created.totalPrice(),
                        "PAID",
                        "ALIPAY",
                        null,
                        null,
                        created.createTime());
        assertTrue(recharges.update(paid));
        assertEquals("PAID", recharges.findByNo(created.rechargeNo()).status());
        assertEquals(new BigDecimal("100.00"), recharges.paidRechargeTotal());
        assertTrue(
                recharges.update(
                        new WalletRecharge(
                                paid.id(),
                                paid.rechargeNo(),
                                paid.requestNo(),
                                paid.walletId(),
                                paid.memberId(),
                                paid.packageId(),
                                paid.payPrice(),
                                paid.bonusPrice(),
                                paid.totalPrice(),
                                "REFUNDED",
                                paid.channelCode(),
                                paid.paidTime(),
                                null,
                                paid.createTime())));
        assertEquals(new BigDecimal("100.00"), recharges.paidRechargeTotal());
    }

    @Test
    void trendUsesOriginalReceiptDateAndExactGrossAmount() {
        var wallet = wallets.createIfAbsent(1L);
        var sql = new JdbcTemplate(context.getBean(JdbcDataSource.class));
        sql.update(
                """
                INSERT INTO pay_payment(payment_no,order_no,channel,amount,status,paid_time) VALUES
                ('P1','O1','ALIPAY',9007199254740993.01,'SUCCESS','2026-11-01 00:00:00'),
                ('P2','O2','WECHAT',0.09,'SUCCESS','2026-11-01 23:59:59'),
                ('P3','O3','ALIPAY',8.00,'WAITING',NULL),
                ('P4','O4','ALIPAY',7.00,'SUCCESS',NULL),
                ('P5','O5','ALIPAY',9.00,'SUCCESS','2026-11-04 00:00:00'),
                ('P6','R1','WECHAT',12.34,'SUCCESS','2026-11-01 18:00:00')
                """);
        sql.update(
                """
                INSERT INTO pay_wallet_recharge
                    (recharge_no,request_no,wallet_id,member_id,pay_price,bonus_price,total_price,status,paid_time,refunded_time)
                VALUES
                ('R1','R1',?,1,12.34,1.00,13.34,'REFUNDING','2026-11-01 18:00:00',NULL),
                ('R2','R2',?,1,0.01,0.01,0.02,'REFUNDED','2026-11-02 15:00:00','2026-11-03 06:00:00'),
                ('R3','R3',?,1,8.00,0.00,8.00,'PENDING',NULL,NULL),
                ('R4','R4',?,1,9.00,0.00,9.00,'PAID',NULL,NULL)
                """,
                wallet.id(),
                wallet.id(),
                wallet.id(),
                wallet.id());
        var trend = new PaymentTrendQueryExe(context.getBean(PaymentTrendGateway.class));
        var from = LocalDate.of(2026, 11, 1);
        var zero = new BigDecimal("0.00");

        assertEquals(
                List.of(
                        new PaymentTrendDayDTO(from, 2, new BigDecimal("9007199254740993.10"),
                                new BigDecimal("12.34")),
                        new PaymentTrendDayDTO(from.plusDays(1), 0, zero, new BigDecimal("0.01")),
                        new PaymentTrendDayDTO(from.plusDays(2), 0, zero, zero)),
                trend.execute(new PaymentTrendQry(from, from.plusDays(2))));
        assertThrows(ServiceException.class, () -> trend.execute(new PaymentTrendQry(from, from.minusDays(1))));
        assertThrows(ServiceException.class, () -> trend.execute(new PaymentTrendQry(from, from.plusDays(366))));
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @ComponentScan(basePackages = {"com.lemnt.pay.infrastructure.payment.gateway"})
    @MapperScan(basePackages = {"com.lemnt.pay.infrastructure.payment.mapper"})
    static class TestConfig {
        @Bean
        JdbcDataSource dataSource() {
            var source = new JdbcDataSource();
            source.setURL(
                    "jdbc:h2:mem:recharge_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
            return source;
        }

        @Bean(destroyMethod = "close")
        Connection schema(JdbcDataSource source) throws Exception {
            var connection = source.getConnection();
            ScriptUtils.executeSqlScript(
                    connection, new ClassPathResource("pay-sql/lemnt_pay.sql"));

            return connection;
        }

        @Bean
        SqlSessionFactory sqlSessionFactory(JdbcDataSource source, Connection schema)
                throws Exception {
            var config = new MybatisConfiguration();
            config.setMapUnderscoreToCamelCase(true);
            var interceptor = new MybatisPlusInterceptor();
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
            config.addInterceptor(interceptor);
            var factory = new MybatisSqlSessionFactoryBean();
            factory.setDataSource(source);
            factory.setConfiguration(config);
            factory.setGlobalConfig(
                    new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
            factory.setMapperLocations(
                    new PathMatchingResourcePatternResolver()
                            .getResources("classpath*:mapper/*.xml"));
            return factory.getObject();
        }

        @Bean
        PlatformTransactionManager transactionManager(JdbcDataSource source) {
            return new DataSourceTransactionManager(source);
        }
    }
}
