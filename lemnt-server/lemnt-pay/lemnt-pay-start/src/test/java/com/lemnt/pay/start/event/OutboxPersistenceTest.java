package com.lemnt.pay.start.event;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.pay.domain.event.gateway.MessageOutboxGateway;
import com.lemnt.pay.infrastructure.event.gateway.MessageOutboxGatewayImpl;
import com.lemnt.pay.infrastructure.event.mapper.MessageOutboxMapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
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
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** 使用仅支付库的生产DDL验证事务消息，禁止依靠跨服务混库通过测试。 */
public class OutboxPersistenceTest {
    JdbcDataSource source;
    Connection schemaConnection;
    JdbcTemplate jdbc;
    TransactionTemplate transaction;
    SqlSessionTemplate session;
    MessageOutboxGateway outbox;

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
                schemaConnection, new ClassPathResource("pay-sql/lemnt_pay.sql"));
        jdbc = new JdbcTemplate(source);
        transaction = new TransactionTemplate(new DataSourceTransactionManager(source));
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setGlobalConfig(
                new GlobalConfig()
                        .setMetaObjectHandler(
                                new MyMetaObjectHandler()));
        factory.setConfiguration(configuration);
        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath*:mapper/*.xml"));
        session = new SqlSessionTemplate(factory.getObject());
        outbox =
                proxy(
                        new MessageOutboxGatewayImpl(session.getMapper(MessageOutboxMapper.class)),
                        MessageOutboxGateway.class);
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

    void enqueue(String payload) {
        transaction.executeWithoutResult(
                status -> outbox.enqueue(CommerceEventTopics.PAY_SUCCESS, "O1:7", "O1", payload));
        // 显式控制数据库到期时间，避免DATETIME秒精度舍入引入等待和偶发失败。
        jdbc.update(
                "UPDATE pay_message_outbox SET next_retry_at=TIMESTAMPADD(SECOND,-1,CURRENT_TIMESTAMP)");
    }

    @Test
    void registrationRollsBackWithBusinessAndRequiresAnExistingTransaction() {
        assertThrows(
                IllegalStateException.class,
                () ->
                        transaction.executeWithoutResult(
                                status -> {
                                    outbox.enqueue(
                                            CommerceEventTopics.PAY_SUCCESS,
                                            "O1:7",
                                            "O1",
                                            "{\"quantity\":2}");
                                    throw new IllegalStateException("模拟业务写入失败");
                                }));
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM pay_message_outbox", Integer.class));
        assertThrows(
                IllegalTransactionStateException.class,
                () -> outbox.enqueue(CommerceEventTopics.PAY_SUCCESS, "O1:7", "O1", "{}"));
    }

    @Test
    void sameBusinessKeepsOriginalEnvelopeAndRejectsChangedFrozenPayload() {
        enqueue("{\"quantity\":2}");
        String first = jdbc.queryForObject("SELECT payload FROM pay_message_outbox", String.class);
        enqueue("{\"quantity\":2}");
        assertEquals(
                first, jdbc.queryForObject("SELECT payload FROM pay_message_outbox", String.class));
        var envelope = JsonUtils.toBean(first, CommerceEventEnvelope.class);
        assertNotNull(envelope.eventNo());
        assertEquals("O1", envelope.aggregateNo());
        assertThrows(IllegalStateException.class, () -> enqueue("{\"quantity\":3}"));
        assertEquals(
                1, jdbc.queryForObject("SELECT COUNT(*) FROM pay_message_outbox", Integer.class));
    }

    @Test
    void expiredLeaseRedeliversOriginalEventAndFencesOldWorker() {
        enqueue("{}");
        var first = outbox.claim(1).get(0);
        assertTrue(outbox.claim(1).isEmpty());
        jdbc.update(
                "UPDATE pay_message_outbox SET lease_until=TIMESTAMPADD(SECOND,-1,CURRENT_TIMESTAMP)");
        var second = outbox.claim(1).get(0);
        assertEquals(first.eventNo(), second.eventNo());
        assertEquals(first.payload(), second.payload());
        assertTrue(second.executeVersion() > first.executeVersion());
        assertFalse(outbox.sent(first));
        outbox.failed(first, "旧线程不能覆盖");
        assertTrue(outbox.sent(second));
        assertEquals(
                "SENT", jdbc.queryForObject("SELECT status FROM pay_message_outbox", String.class));
    }

    @Test
    void exhaustedTaskIsRetainedAndManualRetryKeepsIdentity() {
        enqueue("{}");
        jdbc.update("UPDATE pay_message_outbox SET attempts=19");
        var failed = outbox.claim(1).get(0);
        outbox.failed(failed, "模拟传输失败");
        assertEquals(
                "FAILED",
                jdbc.queryForObject("SELECT status FROM pay_message_outbox", String.class));
        assertTrue(outbox.claim(1).isEmpty());
        assertTrue(outbox.retryFailed(failed.eventNo()));
        jdbc.update(
                "UPDATE pay_message_outbox SET next_retry_at=TIMESTAMPADD(SECOND,-1,CURRENT_TIMESTAMP)");
        var retried = outbox.claim(1).get(0);
        assertEquals(failed.eventNo(), retried.eventNo());
        assertFalse(outbox.sent(failed));
        assertTrue(outbox.sent(retried));
    }
}
