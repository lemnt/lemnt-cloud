package com.lemnt.order.application.delivery;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.order.infrastructure.delivery.gateway.DeliveryManagementLockGatewayImpl;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeliveryManagementLockTest {
    /**
     * 事务提交对独立连接可见后才释放，同一事务不重复获取。
     */
    @Test
    void releaseFollowsRealDatabaseCommit() {
        var source = source();
        var jdbc = new JdbcTemplate(source);
        var locks = mock(LockTemplate.class);
        var handle = mock(LockInfo.class);
        when(locks.lock(anyString(), anyLong(), anyLong(), any())).thenReturn(handle);
        when(locks.releaseLock(handle))
            .thenAnswer(
                call -> {
                    try (var connection = source.getConnection();
                         var statement = connection.createStatement();
                         var rows =
                             statement.executeQuery("SELECT COUNT(*) FROM effect")) {
                        rows.next();
                        assertEquals(1, rows.getInt(1));
                    }
                    return true;
                });
        var gateway = new DeliveryManagementLockGatewayImpl(locks);
        new TransactionTemplate(new DataSourceTransactionManager(source))
            .executeWithoutResult(
                status -> {
                    gateway.lock();
                    gateway.lock();
                    jdbc.update("INSERT INTO effect VALUES(1)");
                    verify(locks, never()).releaseLock(any());
                });
        verify(locks).lock("lemnt:trade:delivery:management", -1, 3000, RedissonLockExecutor.class);
        verify(locks).releaseLock(handle);
    }

    /**
     * 回滚也释放锁，非事务调用不得取锁。
     */
    @Test
    void rejectsOutsideTransactionAndReleasesAfterRollback() {
        var locks = mock(LockTemplate.class);
        var handle = mock(LockInfo.class);
        when(locks.lock(anyString(), anyLong(), anyLong(), any())).thenReturn(handle);
        when(locks.releaseLock(handle)).thenReturn(true);
        var gateway = new DeliveryManagementLockGatewayImpl(locks);
        assertThrows(IllegalStateException.class, gateway::lock);
        assertThrows(
            ServiceException.class,
            () ->
                new TransactionTemplate(new DataSourceTransactionManager(source()))
                    .executeWithoutResult(
                        status -> {
                            gateway.lock();
                            throw new ServiceException("回滚", 409);
                        }));
        verify(locks).releaseLock(handle);
    }

    private JdbcDataSource source() {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:delivery_lock_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        new JdbcTemplate(source).execute("CREATE TABLE effect(id INT PRIMARY KEY)");
        return source;
    }
}
