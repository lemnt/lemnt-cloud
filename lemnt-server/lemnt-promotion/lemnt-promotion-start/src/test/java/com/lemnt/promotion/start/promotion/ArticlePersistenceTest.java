package com.lemnt.promotion.start.promotion;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.promotion.application.article.cmd.ArticleCategorySaveCmd;
import com.lemnt.promotion.application.article.cmd.ArticleSaveCmd;
import com.lemnt.promotion.application.article.qry.ArticlePageQry;
import com.lemnt.promotion.application.article.service.ArticleService;
import com.lemnt.promotion.start.TableTestComponents;

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

/**
 * 生产 SQL/Gateway/事务代理验证 Banner 分页、位置过滤和点击量。
 */
class ArticlePersistenceTest {
    JdbcDataSource source;
    Connection schema;
    ArticleService service;

    @BeforeEach
    void setup() throws Exception {
        source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:article_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        schema = source.getConnection();
        ScriptUtils.executeSqlScript(
                schema, new ClassPathResource("promotion-sql/lemnt_promotion.sql"));
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
        config.addMappers("com.lemnt.promotion.infrastructure");
        service =
                new TableTestComponents(new SqlSessionTemplate(factory), source)
                        .get(ArticleService.class);
    }

    @AfterEach
    void close() throws Exception {
        if (schema != null) {
            schema.close();
        }
    }

    @Test
    void articleStatusRecommendationAndCategoryReferencesUseRealDatabase() {
        var category = service.saveCategory(new ArticleCategorySaveCmd(null, "选购指南", null, 1, 0));
        var hiddenCategory =
                service.saveCategory(new ArticleCategorySaveCmd(null, "草稿分类", null, 2, 1));
        assertEquals(1, service.enabledCategories().size());
        assertEquals(category, service.enabledCategories().get(0).id());
        var first =
                service.save(
                        new ArticleSaveCmd(
                                null,
                                category,
                                6L,
                                "选购须知",
                                "运营",
                                null,
                                "摘要",
                                5,
                                0,
                                true,
                                false,
                                "<p>正文</p>"));
        var hidden =
                service.save(
                        new ArticleSaveCmd(
                                null,
                                hiddenCategory,
                                null,
                                "选购须知",
                                null,
                                null,
                                null,
                                10,
                                1,
                                true,
                                false,
                                "草稿"));
        assertEquals(first, service.published(null, "选购须知").id());
        assertNull(service.published(hidden, null));
        var result = service.page(new ArticlePageQry(null, null, null, true, false, 1, 1), true);
        assertEquals(1, result.getTotal());
        assertEquals(first, result.getRecords().get(0).id());
        assertTrue(service.browse(first));
        assertFalse(service.browse(hidden));
        assertEquals(1L, service.detail(first).browseCount());
        assertThrows(ServiceException.class, () -> service.deleteCategory(category));
        assertThrows(
                ServiceException.class,
                () ->
                        service.save(
                                new ArticleSaveCmd(
                                        null, 999L, null, "缺分类", null, null, null, 1, 0, false,
                                        false, "正文")));
        assertTrue(service.delete(first));
        assertTrue(service.deleteCategory(category));
    }
}
