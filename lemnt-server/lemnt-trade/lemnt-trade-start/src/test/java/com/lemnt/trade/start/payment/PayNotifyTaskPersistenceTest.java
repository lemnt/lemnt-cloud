package com.lemnt.trade.start.payment;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.trade.domain.payment.gateway.PayNotifyTaskGateway;
import com.lemnt.trade.domain.payment.model.PayNotifyLog;
import com.lemnt.trade.domain.payment.model.PayNotifyTask;

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

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** 生产 SQL、DO、Mapper、Gateway 验证任务唯一、CAS 抢占、到期扫描与日志。 */
class PayNotifyTaskPersistenceTest {
    AnnotationConfigApplicationContext context;
    PayNotifyTaskGateway tasks;

    @BeforeEach
    void setup() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        tasks = context.getBean(PayNotifyTaskGateway.class);
    }

    @AfterEach
    void close() {
        context.close();
    }

    private PayNotifyTask task(String bizNo) {
        return new PayNotifyTask(null, "PAY", bizNo, "TR1", "PENDING", 0, 8, null, null, null, null);
    }

    @Test
    void createIsIdempotentByTypeAndBizNo() {
        var first = tasks.createIfAbsent(task("P1"));
        var second = tasks.createIfAbsent(task("P1"));
        assertEquals(first.id(), second.id());
        assertNotNull(tasks.findByBiz("PAY", "P1"));
    }

    @Test
    void claimIsSingleWinnerAndSuccessLeavesDueSet() {
        var created = tasks.createIfAbsent(task("P1"));
        assertTrue(tasks.claim(created.id(), 0));
        assertFalse(tasks.claim(created.id(), 0));
        var now = tasks.databaseNow();
        assertTrue(tasks.finishSuccess(created.id(), 1, "OK", now));
        assertEquals("SUCCESS", tasks.find(created.id()).status());
        assertTrue(tasks.findDue(now.plusHours(1), 50).isEmpty());
    }

    @Test
    void failedTaskIsDueAfterNextTimeAndKeepsLogs() {
        var created = tasks.createIfAbsent(task("P1"));
        assertTrue(tasks.claim(created.id(), 0));
        var now = LocalDateTime.of(2026, 1, 1, 0, 0);
        var next = now.plusSeconds(15);
        assertTrue(tasks.finishFailed(created.id(), 1, "核券失败", next, now));
        assertTrue(tasks.findDue(now, 50).isEmpty());
        assertEquals(1, tasks.findDue(next, 50).size());
        tasks.appendLog(new PayNotifyLog(null, created.id(), 1, "FAILED", "核券失败", null));
        var logs = tasks.logs(created.id());
        assertEquals(1, logs.size());
        assertEquals("FAILED", logs.get(0).status());
        assertTrue(tasks.resetForRetry(created.id(), now));
        var reset = tasks.find(created.id());
        assertEquals("PENDING", reset.status());
        assertEquals(0, reset.notifyTimes());
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @ComponentScan(basePackages = {"com.lemnt.trade.infrastructure.payment.gateway"})
    @MapperScan(basePackages = {"com.lemnt.trade.infrastructure.payment.mapper"})
    static class TestConfig {
        @Bean
        JdbcDataSource dataSource() {
            var source = new JdbcDataSource();
            source.setURL("jdbc:h2:mem:pay_notify_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
            return source;
        }

        @Bean(destroyMethod = "close")
        Connection schema(JdbcDataSource source) throws Exception {
            var connection = source.getConnection();
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("trade-sql/V11__pay_notify_task.sql"));
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
