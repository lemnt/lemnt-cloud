package com.lemnt.product.start.event;

import com.lemnt.product.domain.event.gateway.MessageInboxGateway;
import com.lemnt.product.infrastructure.event.gateway.MessageInboxGatewayImpl;
import com.lemnt.product.infrastructure.event.mapper.MessageInboxMapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消费端幂等必须与业务同事务，并识别同号不同冻结载荷。
 * 使用仅商品库的生产DDL验证收件台账，禁止依靠跨服务混库通过测试。
 */
class InboxPersistenceTest {
    JdbcDataSource source;
    Connection schemaConnection;
    JdbcTemplate jdbc;
    TransactionTemplate transaction;
    SqlSessionTemplate session;

    @BeforeEach
    void setup() throws Exception {
        source = new JdbcDataSource();
        source.setURL(
            "jdbc:h2:mem:event_"
                + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000");
        // H2 2.4的CHECK IN常量比较器依赖建表会话，保留至本用例结束。
        schemaConnection = source.getConnection();
        ScriptUtils.executeSqlScript(
            schemaConnection, new ClassPathResource("product-sql/lemnt_product.sql"));
        jdbc = new JdbcTemplate(source);
        transaction = new TransactionTemplate(new DataSourceTransactionManager(source));
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        factory.setMapperLocations(
            new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/MessageInboxMapper.xml"));
        session = new SqlSessionTemplate(factory.getObject());
    }

    @AfterEach
    void closeSchemaConnection() throws Exception {
        if (schemaConnection != null) schemaConnection.close();
    }

    <T> T proxy(Object target, Class<T> type) {
        var proxy = new ProxyFactory(target);
        proxy.addAdvice(
            new TransactionInterceptor(
                new DataSourceTransactionManager(source),
                new AnnotationTransactionAttributeSource()));
        return type.cast(proxy.getProxy());
    }

    MessageInboxGateway inbox() {
        return proxy(
            new MessageInboxGatewayImpl(session.getMapper(MessageInboxMapper.class)),
            MessageInboxGateway.class);
    }

    @Test
    void failedBusinessRollsBackReceiptAndAllowsOriginalEventToRetry() {
        var inbox = inbox();
        assertThrows(
            IllegalStateException.class,
            () ->
                transaction.executeWithoutResult(
                    status -> {
                        assertTrue(
                            inbox.begin(
                                "stock.refund",
                                "E1",
                                "AFTER_SALE_COMPLETED",
                                "AF1",
                                "{}"));
                        throw new IllegalStateException("模拟库存写入失败");
                    }));
        assertEquals(
            0,
            jdbc.queryForObject("SELECT COUNT(*) FROM product_message_inbox", Integer.class));
        transaction.executeWithoutResult(
            status ->
                assertTrue(
                    inbox.begin(
                        "stock.refund",
                        "E1",
                        "AFTER_SALE_COMPLETED",
                        "AF1",
                        "{}")));
        transaction.executeWithoutResult(
            status ->
                assertFalse(
                    inbox.begin(
                        "stock.refund",
                        "E1",
                        "AFTER_SALE_COMPLETED",
                        "AF1",
                        "{}")));
    }

    @Test
    void businessDuplicateIsDeduplicatedButChangedContentOrEventIdentityFails() {
        var inbox = inbox();
        transaction.executeWithoutResult(
            status ->
                assertTrue(
                    inbox.begin(
                        "stock.refund",
                        "E1",
                        "AFTER_SALE_COMPLETED",
                        "AF1",
                        "{}")));
        transaction.executeWithoutResult(
            status ->
                assertFalse(
                    inbox.begin(
                        "stock.refund",
                        "E2",
                        "AFTER_SALE_COMPLETED",
                        "AF1",
                        "{}")));
        assertThrows(
            IllegalStateException.class,
            () ->
                transaction.executeWithoutResult(
                    status ->
                        inbox.begin(
                            "stock.refund",
                            "E1",
                            "AFTER_SALE_COMPLETED",
                            "AF1",
                            "{\"quantity\":2}")));
        assertThrows(
            IllegalStateException.class,
            () ->
                transaction.executeWithoutResult(
                    status ->
                        inbox.begin(
                            "stock.refund", "E1", "ORDER_PAID", "O2", "{}")));
    }

    @Test
    void concurrentBusinessDuplicateOnlyAppliesOneEffect() throws Exception {
        var inbox = inbox();
        jdbc.execute("CREATE TABLE test_effect(id INT PRIMARY KEY)");
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first =
                pool.submit(
                    () -> {
                        start.await();
                        transaction.executeWithoutResult(
                            status -> {
                                if (inbox.begin(
                                    "stock.refund",
                                    "E1",
                                    "AFTER_SALE_COMPLETED",
                                    "AF1",
                                    "{}"))
                                    jdbc.update("INSERT INTO test_effect VALUES(1)");
                            });
                        return true;
                    });
            var second =
                pool.submit(
                    () -> {
                        start.await();
                        transaction.executeWithoutResult(
                            status -> {
                                if (inbox.begin(
                                    "stock.refund",
                                    "E2",
                                    "AFTER_SALE_COMPLETED",
                                    "AF1",
                                    "{}"))
                                    jdbc.update("INSERT INTO test_effect VALUES(1)");
                            });
                        return true;
                    });
            start.countDown();
            assertTrue(first.get(10, TimeUnit.SECONDS));
            assertTrue(second.get(10, TimeUnit.SECONDS));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM test_effect", Integer.class));
            assertEquals(
                1,
                jdbc.queryForObject(
                    "SELECT COUNT(*) FROM product_message_inbox", Integer.class));
        } finally {
            pool.shutdownNow();
        }
    }
}
