package com.lemnt.trade.infrastructure.mapper;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Executes production mapper SQL; H2 does not replace required MySQL integration verification. */
class TradeOutboxMapperTest {
    JdbcDataSource dataSource;
    org.apache.ibatis.session.SqlSessionFactory factory;

    @BeforeEach
    void setup() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE trade_outbox (id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "event_id VARCHAR(64), aggregate_no VARCHAR(40), event_type VARCHAR(64), payload VARCHAR(500), "
                    + "status VARCHAR(20), retry_count INT DEFAULT 0, lease_token VARCHAR(36), lease_until TIMESTAMP, "
                    + "next_attempt_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP)");
        }
        Configuration config = new Configuration(new Environment("test", new JdbcTransactionFactory(), dataSource));
        config.setMapUnderscoreToCamelCase(true);
        config.addMapper(TradeOutboxMapper.class);
        factory = new SqlSessionFactoryBuilder().build(config);
    }

    void sql(String sql) throws Exception {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    void insert(String id, String status, int attempts) throws Exception {
        sql("INSERT INTO trade_outbox(event_id,status,retry_count) VALUES ('" + id + "','" + status + "'," + attempts + ")");
    }

    @Test
    void leaseTakeoverFencesStaleOwner() throws Exception {
        insert("E1", "NEW", 0);
        try (SqlSession session = factory.openSession(true)) {
            var mapper = session.getMapper(TradeOutboxMapper.class);
            assertEquals(1, mapper.claim("E1", "old"));
            assertEquals(0, mapper.claim("E1", "new"));
            sql("UPDATE trade_outbox SET lease_until=TIMESTAMP '2000-01-01 00:00:00'");
            assertEquals(0, mapper.delivered("E1", "old"));
            assertEquals(1, mapper.claim("E1", "new"));
            assertEquals(0, mapper.failed("E1", "old", 30));
            assertEquals(0, mapper.delivered("E1", "old"));
            assertEquals(1, mapper.delivered("E1", "new"));
            assertTrue(mapper.pending(10).isEmpty());
        }
    }

    @Test
    void dueSelectionDoesNotStarveBehindDelayedAndActiveEvents() throws Exception {
        insert("delayed", "FAILED", 1);
        insert("busy", "PROCESSING", 1);
        insert("ready", "NEW", 0);
        sql("UPDATE trade_outbox SET next_attempt_at=TIMESTAMP '2099-01-01 00:00:00' WHERE event_id='delayed'");
        sql("UPDATE trade_outbox SET lease_until=TIMESTAMP '2099-01-01 00:00:00' WHERE event_id='busy'");
        try (var session = factory.openSession(true)) {
            var mapper = session.getMapper(TradeOutboxMapper.class);
            assertEquals("ready", mapper.pending(1).get(0).getEventId());
            assertEquals(0, mapper.claim("delayed", "token"));
        }
    }

    @Test
    void tenthFailureIsDeadAndOnlyDeadCanReplay() throws Exception {
        insert("E1", "FAILED", 9);
        try (var session = factory.openSession(true)) {
            var mapper = session.getMapper(TradeOutboxMapper.class);
            assertEquals(0, mapper.replayDead("E1"));
            assertEquals(1, mapper.claim("E1", "token"));
            assertEquals(1, mapper.failed("E1", "token", 60));
            assertTrue(mapper.pending(1).isEmpty());
            assertEquals(0, mapper.claim("E1", "other"));
            assertEquals(1, mapper.replayDead("E1"));
            assertEquals(0, mapper.pending(1).get(0).getRetryCount());
        }
    }

    @Test
    void crashedFinalAttemptIsExhaustedAfterLeaseExpiry() throws Exception {
        insert("E1", "PROCESSING", 10);
        sql("UPDATE trade_outbox SET lease_until=TIMESTAMP '2000-01-01 00:00:00'");
        try (var session = factory.openSession(true)) {
            var mapper = session.getMapper(TradeOutboxMapper.class);
            assertEquals(1, mapper.exhaust());
            assertEquals(1, mapper.replayDead("E1"));
        }
    }
}
