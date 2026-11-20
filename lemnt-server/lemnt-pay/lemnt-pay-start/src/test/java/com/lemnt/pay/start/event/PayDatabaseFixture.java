package com.lemnt.pay.start.event;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.pay.infrastructure.payment.mapper.PayNotifyLogMapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

/**
 * 使用仅支付库的生产DDL验证本地事务，禁止依靠跨服务混库通过测试。
 */
abstract class PayDatabaseFixture {
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
                schemaConnection, new ClassPathResource("pay-sql/lemnt_pay.sql"));
        jdbc = new JdbcTemplate(source);
        transaction = new TransactionTemplate(new DataSourceTransactionManager(source));
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setGlobalConfig(new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
        factory.setConfiguration(configuration);
        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml"));
        var sessionFactory = factory.getObject();
        sessionFactory.getConfiguration().addMapper(PayNotifyLogMapper.class);
        session = new SqlSessionTemplate(sessionFactory);
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
}
