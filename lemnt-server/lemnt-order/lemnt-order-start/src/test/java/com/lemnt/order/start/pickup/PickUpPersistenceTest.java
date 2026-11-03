package com.lemnt.order.start.pickup;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.order.application.pickup.cmd.PickUpAssignCmd;
import com.lemnt.order.application.pickup.cmd.PickUpBindCmd;
import com.lemnt.order.application.pickup.cmd.PickUpStoreSaveCmd;
import com.lemnt.order.application.pickup.cmd.PickUpVerifyCmd;
import com.lemnt.order.application.pickup.service.PickUpOrderService;
import com.lemnt.order.application.pickup.service.PickUpStoreService;
import com.lemnt.order.start.TableTestComponents;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** 生产 SQL/Gateway/事务代理验证自提门店、核销码、店员权限和核销完成。 */
class PickUpPersistenceTest {
    JdbcDataSource source;
    Connection schema;
    PickUpStoreService stores;
    PickUpOrderService orders;

    @BeforeEach
    void setup() throws Exception {
        source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:pickup_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        schema = source.getConnection();
        for (String path :
                List.of(
                        "order-sql/lemnt_order.sql",
                        "order-sql/lemnt_order.sql",
                        "order-sql/lemnt_order.sql",
                        "order-sql/lemnt_order.sql",
                        "order-sql/lemnt_order.sql",
                        "order-sql/lemnt_order.sql")) {
            ScriptUtils.executeSqlScript(schema, new ClassPathResource(path));
        }
        var config = new MybatisConfiguration();
        config.setMapUnderscoreToCamelCase(true);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        config.addInterceptor(interceptor);
        var bean = new MybatisSqlSessionFactoryBean();
        bean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml"));
        bean.setDataSource(source);
        bean.setGlobalConfig(new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
        bean.setConfiguration(config);
        var factory = bean.getObject();
        config.addMappers("com.lemnt.order.infrastructure");
        var components = new TableTestComponents(new SqlSessionTemplate(factory), source);
        stores = components.get(PickUpStoreService.class);
        orders = components.get(PickUpOrderService.class);
    }

    @AfterEach
    void close() throws Exception {
        if (schema != null) {
            schema.close();
        }
    }

    Long createStore(List<Long> verifyUserIds) {
        return stores.create(
                new PickUpStoreSaveCmd(
                        null,
                        "中心店",
                        "门店简介",
                        "13800000000",
                        1L,
                        "中心路1号",
                        null,
                        "09:00:00",
                        "21:00:00",
                        null,
                        null,
                        verifyUserIds,
                        0));
    }

    void insertOrder(String orderNo) throws Exception {
        try (var statement = schema.createStatement()) {
            statement.execute(
                    "INSERT INTO order_info(order_no,member_id,total_amount,status) VALUES('"
                            + orderNo
                            + "',7,10.00,'PAID')");
        }
    }

    @Test
    void assignGeneratesCodeAndOnlyBoundClerkCanVerify() throws Exception {
        var storeId = createStore(List.of(1L));
        insertOrder("TR1");
        var assigned = orders.assign(new PickUpAssignCmd("TR1", storeId, 99L));
        assertTrue(assigned.verifyCode().matches("\\d{8}"));
        assertEquals("中心店", orders.preview(assigned.verifyCode()).storeName());
        assertThrows(
                ServiceException.class,
                () -> orders.verify(new PickUpVerifyCmd(assigned.verifyCode(), 2L)));
        var done = orders.verify(new PickUpVerifyCmd(assigned.verifyCode(), 1L));
        assertEquals("COMPLETED", done.status());
        try (var statement = schema.createStatement();
                var result =
                        statement.executeQuery(
                                "SELECT status,receive_time FROM order_info WHERE order_no='TR1'")) {
            result.next();
            assertEquals("COMPLETED", result.getString(1));
            assertNotNull(result.getTimestamp(2));
        }
    }

    @Test
    void storeListOnlyReturnsEnabledAndBindingRejectsDuplicates() {
        var storeId = createStore(List.of(1L));
        assertEquals(1, stores.appList().size());
        stores.update(
                new PickUpStoreSaveCmd(
                        storeId,
                        "中心店",
                        null,
                        "13800000000",
                        1L,
                        "中心路1号",
                        null,
                        "09:00:00",
                        "21:00:00",
                        null,
                        null,
                        List.of(1L),
                        1));
        assertTrue(stores.appList().isEmpty());
        assertThrows(
                ServiceException.class,
                () -> stores.bind(new PickUpBindCmd(storeId, List.of(1L, 1L))));
    }

    @Test
    void overnightHoursArePersistedAndReturned() {
        var id =
                stores.create(
                        new PickUpStoreSaveCmd(
                                null,
                                "夜间店",
                                null,
                                "1",
                                1L,
                                "地址",
                                null,
                                "21:00:00",
                                "09:00:00",
                                null,
                                null,
                                List.of(),
                                0));
        assertEquals("21:00:00", stores.detail(id).openingTime());
        assertEquals("09:00:00", stores.detail(id).closingTime());
    }

    @Test
    void invalidTimeAndDeletingMissingStoreAreRejected() {
        assertThrows(
                ServiceException.class,
                () ->
                        stores.create(
                                new PickUpStoreSaveCmd(
                                        null,
                                        "店",
                                        null,
                                        "1",
                                        1L,
                                        "地址",
                                        null,
                                        "25:00:00",
                                        "09:00:00",
                                        null,
                                        null,
                                        List.of(),
                                        0)));
        assertThrows(ServiceException.class, () -> stores.delete(999L));
    }
}
