package com.lemnt.mall.start.promotion;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.mall.application.promotion.cmd.BannerCreateCmd;
import com.lemnt.mall.application.promotion.cmd.BannerUpdateCmd;
import com.lemnt.mall.application.promotion.qry.BannerPageQry;
import com.lemnt.mall.application.promotion.service.BannerManagementService;
import com.lemnt.mall.start.TableTestComponents;

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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** 生产 SQL/Gateway/事务代理验证 Banner 分页、位置过滤和点击量。 */
class BannerPersistenceTest {
    JdbcDataSource source;
    Connection schema;
    BannerManagementService service;

    @BeforeEach
    void setup() throws Exception {
        source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:banner_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        schema = source.getConnection();
        ScriptUtils.executeSqlScript(
                schema, new ClassPathResource("sql/V25__promotion_banner.sql"));
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
        config.addMappers("com.lemnt.mall.infrastructure");
        service =
                new TableTestComponents(new SqlSessionTemplate(factory), source)
                        .get(BannerManagementService.class);
    }

    @AfterEach
    void close() throws Exception {
        if (schema != null) {
            schema.close();
        }
    }

    Long create(String title, int position, int sort, int status) {
        return service.create(
                new BannerCreateCmd(
                        title,
                        "/pages/store/index",
                        "https://example.test/" + title + ".png",
                        sort,
                        status,
                        position,
                        null));
    }

    @Test
    void appListFiltersStatusAndOrdersBySort() {
        var first = create("first", 1, 1, 0);
        var second = create("second", 1, 9, 0);
        create("hidden", 1, 99, 1);
        create("other", 2, 5, 0);
        var list = service.appList(1);
        assertEquals(2, list.size());
        assertEquals(second, list.get(0).id());
        assertEquals(first, list.get(1).id());
        assertEquals(1, service.appList(2).size());
    }

    @Test
    void pageFiltersTitleAndStatusAndUpdateKeepsBrowseCount() {
        var id = create("spring", 1, 0, 0);
        create("summer", 1, 0, 1);
        var page = service.page(new BannerPageQry("s", 0, 1, 20));
        assertEquals(1, page.getTotal());
        assertEquals(id, page.getRecords().get(0).id());
        service.addBrowseCount(id);
        service.addBrowseCount(id);
        assertEquals(2, service.detail(id).browseCount());
        service.update(
                new BannerUpdateCmd(
                        id,
                        "spring-new",
                        "/pages/store/index",
                        "https://example.test/new.png",
                        3,
                        1,
                        2,
                        "备注"));
        var updated = service.detail(id);
        assertEquals("spring-new", updated.title());
        assertEquals(3, updated.sort());
        assertEquals(2, updated.position());
        assertEquals(2, updated.browseCount());
        service.delete(id);
        assertThrows(
                com.lemnt.common.core.exception.ServiceException.class, () -> service.detail(id));
    }

    @Test
    void invalidPositionStatusAndBrowseTargetAreRejected() {
        assertThrows(
                com.lemnt.common.core.exception.ServiceException.class,
                () -> create("bad", 6, 0, 0));
        assertThrows(
                com.lemnt.common.core.exception.ServiceException.class,
                () -> service.create(new BannerCreateCmd("bad", "/x", "https://x", 0, 2, 1, null)));
        assertThrows(
                com.lemnt.common.core.exception.ServiceException.class,
                () -> service.addBrowseCount(1L));
    }
}
