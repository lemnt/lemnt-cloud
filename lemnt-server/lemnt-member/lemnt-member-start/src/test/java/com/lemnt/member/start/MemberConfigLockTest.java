package com.lemnt.member.start;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.infrastructure.growth.gateway.MemberConfigLockGatewayImpl;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MemberConfigLockTest {
    /** 事务提交对独立连接可见后才释放，同一事务不重复获取。 */
    @Test
    void releaseFollowsRealDatabaseCommit() {
        var source = source();
        var jdbc = new JdbcTemplate(source);
        var locks = mock(LockTemplate.class);
        var handle = mock(LockInfo.class);
        when(locks.lock(anyString(), anyLong(), anyLong(), any())).thenReturn(handle);
        when(locks.releaseLock(handle)).thenAnswer(call -> {
            try (var connection = source.getConnection();
                    var statement = connection.createStatement();
                    var rows = statement.executeQuery("SELECT COUNT(*) FROM effect")) {
                rows.next();
                assertEquals(1, rows.getInt(1));
            }
            return true;
        });
        var gateway = new MemberConfigLockGatewayImpl(locks);
        new TransactionTemplate(new DataSourceTransactionManager(source)).executeWithoutResult(status -> {
            gateway.lockConfiguration();
            gateway.lockConfiguration();
            jdbc.update("INSERT INTO effect VALUES(1)");
            verify(locks, never()).releaseLock(any());
        });
        verify(locks).lock("lemnt:member:growth:configuration", -1, 3000, RedissonLockExecutor.class);
        verify(locks).releaseLock(handle);
    }

    /** 回滚也在数据丢弃后释放，不留下业务配置锁。 */
    @Test
    void releaseAlsoFollowsRollback() {
        var source = source();
        var jdbc = new JdbcTemplate(source);
        var support = new MemberConfigLockTestSupport();
        assertThrows(ServiceException.class, () -> new TransactionTemplate(new DataSourceTransactionManager(source))
                .executeWithoutResult(status -> {
                    support.gateway.lockConfiguration();
                    jdbc.update("INSERT INTO effect VALUES(1)");
                    verify(support.locks, never()).releaseLock(any());
                    throw new ServiceException("回滚", 409);
                }));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM effect", Integer.class));
        verify(support.locks).releaseLock(any());
        new TransactionTemplate(new DataSourceTransactionManager(source))
                .executeWithoutResult(status -> support.gateway.lockConfiguration());
    }

    /** 非事务调用不取锁，获取失败统一409且不执行后续配置写入。 */
    @Test
    void rejectsOutsideTransactionAndUnavailableLock() {
        var locks = mock(LockTemplate.class);
        var gateway = new MemberConfigLockGatewayImpl(locks);
        assertThrows(IllegalStateException.class, gateway::lockConfiguration);
        verifyNoInteractions(locks);
        var error = assertThrows(ServiceException.class, () -> new TransactionTemplate(new DataSourceTransactionManager(source()))
                .executeWithoutResult(status -> gateway.lockConfiguration()));
        assertEquals(409, error.getCode());
        verify(locks, never()).releaseLock(any());
    }

    private JdbcDataSource source() {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:config_lock_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        new JdbcTemplate(source).execute("CREATE TABLE effect(id INT PRIMARY KEY)");
        return source;
    }
}
