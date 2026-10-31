package com.lemnt.auth.start;

import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;

/** 认证中心的启动类路径不应再引入数据库运行组件。 */
class AuthPersistenceBoundaryTest {
    @Test
    void authDoesNotRequireJdbcDriverOrMyBatisRuntime() {
        var loader = getClass().getClassLoader();
        for (String type : new String[] {
                "org.springframework.jdbc.core.JdbcTemplate",
                "org.apache.ibatis.session.SqlSessionFactory",
                "com.mysql.cj.jdbc.Driver",
                "com.zaxxer.hikari.HikariDataSource",
                "org.h2.Driver"}) {
            assertFalse(ClassUtils.isPresent(type, loader), type);
        }
    }
}
