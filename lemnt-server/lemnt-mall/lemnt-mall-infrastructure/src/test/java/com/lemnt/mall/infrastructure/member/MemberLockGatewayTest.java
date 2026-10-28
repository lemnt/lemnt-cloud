package com.lemnt.mall.infrastructure.member;

import com.lemnt.mall.infrastructure.member.gateway.MemberLockGatewayImpl;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** 验证基础锁模板与真实事务的释放时机，不替代 Redis 集群验收。 */
class MemberLockGatewayTest {
    LockTemplate locks;
    LockInfo lock;
    MemberLockGatewayImpl gateway;
    TransactionTemplate transaction;
    JdbcDataSource source;
    JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        locks = mock(LockTemplate.class);
        lock = mock(LockInfo.class);
        gateway = new MemberLockGatewayImpl(locks);
        source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:member_lock_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE operation_result(id INT PRIMARY KEY)");
        transaction = new TransactionTemplate(new DataSourceTransactionManager(source));
        when(locks.lock(anyString(), eq(-1L), eq(3000L), eq(RedissonLockExecutor.class))).thenReturn(lock);
        when(locks.releaseLock(any())).thenReturn(true);
    }

    @Test
    void releaseHappensAfterTheCommitIsVisible() {
        when(locks.releaseLock(lock)).thenAnswer(invocation -> {
            // 用独立连接确认真正提交后才解锁，不复用事务中的连接。
            try (var connection = source.getConnection();
                    var statement = connection.createStatement();
                    var rows = statement.executeQuery("SELECT COUNT(*) FROM operation_result")) {
                rows.next();
                assertEquals(1, rows.getInt(1));
            }
            return true;
        });
        transaction.executeWithoutResult(status -> {
            gateway.lockMember(1L);
            jdbc.update("INSERT INTO operation_result VALUES(1)");
            verify(locks, never()).releaseLock(any());
        });
        verify(locks).releaseLock(lock);
    }

    @Test
    void rollbackAlsoReleasesTheLockAfterDataIsDiscarded() {
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            gateway.lockMember(1L);
            jdbc.update("INSERT INTO operation_result VALUES(1)");
            verify(locks, never()).releaseLock(any());
            throw new IllegalStateException("rollback");
        }));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM operation_result", Integer.class));
        verify(locks).releaseLock(lock);
    }

    @Test
    void repeatedAcquisitionInOneTransactionUsesOneLock() {
        transaction.executeWithoutResult(status -> {
            gateway.lockMember(1L);
            gateway.lockMember(1L);
            verify(locks, never()).releaseLock(any());
        });
        verify(locks).lock("lemnt:mall:member:operation:1", -1, 3000, RedissonLockExecutor.class);
        verify(locks).releaseLock(lock);
    }

    @Test
    void nestedNewTransactionReleasesOnlyItsOwnAcquisition() {
        var innerLock = mock(LockInfo.class);
        when(locks.lock(anyString(), anyLong(), anyLong(), any())).thenReturn(lock, innerLock);
        var inner = new TransactionTemplate(new DataSourceTransactionManager(source));
        inner.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.executeWithoutResult(status -> {
            gateway.lockMember(1L);
            inner.executeWithoutResult(innerStatus -> gateway.lockMember(1L));
            verify(locks).releaseLock(innerLock);
            verify(locks, never()).releaseLock(lock);
            gateway.lockMember(1L);
        });
        verify(locks).releaseLock(lock);
    }

    @Test
    void lockFailureAbortsWithoutRegisteringARelease() {
        when(locks.lock(anyString(), anyLong(), anyLong(), any())).thenReturn(null);
        assertThrows(IllegalStateException.class,
                () -> transaction.executeWithoutResult(status -> gateway.lockMember(1L)));
        verify(locks, never()).releaseLock(any());
    }

    @Test
    void acquisitionWithoutATransactionIsRejected() {
        assertThrows(IllegalStateException.class, () -> gateway.lockMember(1L));
        verifyNoInteractions(locks);
    }
}
