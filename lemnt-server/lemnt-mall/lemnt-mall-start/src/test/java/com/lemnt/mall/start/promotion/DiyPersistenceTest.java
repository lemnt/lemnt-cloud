package com.lemnt.mall.start.promotion;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.mall.application.promotion.cmd.DiyPageSaveCmd;
import com.lemnt.mall.application.promotion.cmd.DiyPropertyCmd;
import com.lemnt.mall.application.promotion.cmd.DiyTemplateSaveCmd;
import com.lemnt.mall.application.promotion.qry.DiyPagePageQry;
import com.lemnt.mall.application.promotion.service.DiyDecorationService;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** 生产 SQL/Gateway/事务代理验证装修模板默认页面、启用切换与属性校验。 */
class DiyPersistenceTest {
    JdbcDataSource source;
    Connection schema;
    DiyDecorationService service;

    @BeforeEach
    void setup() throws Exception {
        source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:diy_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        schema = source.getConnection();
        ScriptUtils.executeSqlScript(schema, new ClassPathResource("sql/V27__promotion_diy.sql"));
        var config = new MybatisConfiguration();
        config.setMapUnderscoreToCamelCase(true);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        config.addInterceptor(interceptor);
        var bean = new MybatisSqlSessionFactoryBean();
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml"));
        bean.setDataSource(source);
        bean.setGlobalConfig(new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
        bean.setConfiguration(config);
        var factory = bean.getObject();
        config.addMappers("com.lemnt.mall.infrastructure");
        service = new TableTestComponents(new SqlSessionTemplate(factory), source)
                .get(DiyDecorationService.class);
    }

    @AfterEach
    void close() throws Exception {
        if (schema != null) {
            schema.close();
        }
    }

    Long template(String name) {
        return service.createTemplate(new DiyTemplateSaveCmd(null, name, "备注",
                List.of("https://example.test/preview.png")));
    }

    @Test
    void createTemplateAddsHomeAndMyPages() {
        var id = template("默认模板");
        var detail = service.templateDetail(id);
        assertFalse(detail.used());
        var pages = service.pagePage(new DiyPagePageQry(id, null, 1, 20));
        assertEquals(2, pages.getTotal());
        assertEquals(List.of("首页", "我的"), pages.getRecords().stream().map(row -> row.name()).toList());
    }

    @Test
    void useTemplateKeepsOnlyOneUsedAndUsedCannotBeDeleted() {
        var first = template("模板一");
        var second = template("模板二");
        assertTrue(service.useTemplate(first));
        assertTrue(service.useTemplate(second));
        assertFalse(service.templateDetail(first).used());
        assertTrue(service.templateDetail(second).used());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> service.deleteTemplate(second));
        assertTrue(service.deleteTemplate(first));
    }

    @Test
    void pageNamesAreUniqueAndPropertiesMustBeJson() {
        var id = template("模板");
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> service.createPage(new DiyPageSaveCmd(null, id, "首页", null, List.of())));
        var page = service.createPage(new DiyPageSaveCmd(null, id, "活动页", null, List.of()));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> service.updatePage(new DiyPageSaveCmd(page, id, "我的", null, List.of())));
        assertTrue(service.updatePageProperty(new DiyPropertyCmd(page, "{\"components\":[]}")));
        assertEquals("{\"components\":[]}", service.pageDetail(page).property());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> service.updatePageProperty(new DiyPropertyCmd(page, "not-json")));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> service.updateTemplateProperty(new DiyPropertyCmd(id, "not-json")));
        assertTrue(service.updateTemplateProperty(new DiyPropertyCmd(id, "{\"tabbar\":{}}")));
    }

    @Test
    void usedTemplateReturnsHomeAndUserProperties() {
        var id = template("启用模板");
        service.useTemplate(id);
        var pages = service.pagePage(new DiyPagePageQry(id, null, 1, 20)).getRecords();
        var home = pages.stream().filter(row -> row.name().equals("首页")).findFirst().orElseThrow();
        service.updatePageProperty(new DiyPropertyCmd(home.id(), "{\"components\":[{\"id\":\"TitleBar\"}]}"));
        var used = service.usedTemplate();
        assertEquals(id, used.id());
        assertTrue(used.home().contains("TitleBar"));
        assertEquals("{}", used.user());
        assertEquals(home.id(), service.appPage(home.id()).id());
    }

    @Test
    void templateNamesAreUnique() {
        template("重复");
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> template("重复"));
    }
}
