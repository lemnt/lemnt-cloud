package com.lemnt.mall.start.product;

import com.lemnt.mall.application.promotion.article.service.ArticleService;
import com.lemnt.mall.application.promotion.article.service.ArticleCategoryService;
import com.lemnt.mall.application.promotion.article.cmd.*;
import com.lemnt.mall.application.promotion.article.qry.*;
import com.lemnt.common.core.exception.ServiceException;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** 文章生产DDL、真实Mapper/Gateway/事务验证，不连接线上数据库。 */
class ArticleContentPersistenceTest {
    CatalogPersistenceTest fixture;
    ArticleService articles;
    ArticleCategoryService categories;
    Long category;

    @BeforeEach
    void setup() throws Exception {
        fixture = new CatalogPersistenceTest();
        fixture.setup();
        ScriptUtils.executeSqlScript(fixture.schemaConnection, new ClassPathResource("sql/V18__promotion_article.sql"));
        articles = fixture.components.get(ArticleService.class);
        categories = fixture.components.get(ArticleCategoryService.class);
        category = categories.create(new ArticleCategoryCreateCmd("公告", null, 0, 2));
    }

    @AfterEach
    void close() throws Exception {
        fixture.closeSchemaConnection();
    }

    private ArticleCreateCmd draft(String title, boolean hot) {
        return new ArticleCreateCmd(category, 0L, title, "作者", "https://example.test/a.png", null, 1, 0, hot, false, "<p>正文</p>");
    }

    @Test
    void categoryReferencesAndLogicalDeletionFollowRealDatabase() throws Exception {
        Long article = articles.create(draft("介绍", false));
        assertEquals(409, assertThrows(ServiceException.class, () -> categories.delete(category)).getCode());
        assertTrue(articles.delete(article));
        assertTrue(categories.delete(category));
        assertThrows(ServiceException.class, () -> articles.create(draft("孤立", false)));
        try (var statement = fixture.schemaConnection.createStatement();
             var rows = statement.executeQuery("SELECT deleted FROM mall_promotion_article WHERE id=" + article)) {
            assertTrue(rows.next());
            assertTrue(rows.getBoolean(1));
        }
    }

    @Test
    void atomicBrowseCountAndMetadataUpdatesDoNotLoseCounts() throws Exception {
        Long article = articles.create(draft("介绍", false));
        var pool = Executors.newFixedThreadPool(6);
        try {
            var tasks = java.util.stream.IntStream.range(0, 24).mapToObj(i -> pool.submit(() -> articles.increment(article))).toList();
            for (var task : tasks) assertTrue(task.get(10, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
        assertEquals(24, articles.detail(article).browseCount());
        articles.update(new ArticleUpdateCmd(article, category, 0L, "新标题", null, "https://example.test/b.png", "简介", 0, 1, false, true, "<p>新内容</p>"));
        assertEquals(24, articles.detail(article).browseCount());
        assertNull(articles.detail(article).author());
        assertEquals(1, articles.read(article, null).status()); // 源文章公开查询没有状态过滤。
    }

    @Test
    void exactTitleAndFalseFiltersAndCategoryOrderArePreserved() {
        Long first = articles.create(draft("同名", false));
        Long last = articles.create(draft("同名", true));
        assertEquals(last, articles.read(null, "同名").id());
        assertEquals(first, articles.read(first, "同名").id());
        assertNull(articles.read(null, "不存在"));
        assertThrows(ServiceException.class, () -> articles.read(null, null));
        assertEquals(first, articles.recommended(false, false).get(0).id());
        categories.create(new ArticleCategoryCreateCmd("禁用", null, 1, 0));
        Long earlier = categories.create(new ArticleCategoryCreateCmd("先展示", null, 0, 1));
        assertEquals(java.util.List.of(earlier, category), categories.enabled().stream().map(v -> v.id()).toList());
        assertEquals(0, articles.page(new ArticlePageQry(null,null,"同",null,null,null,null,null,null,1,20)).getTotal());
        assertEquals(1, articles.page(new ArticlePageQry(null,null,null,null,0,false,false,null,null,1,20)).getTotal());
    }

    @Test
    void invalidWritesAndMissingRecordsDoNotMutateData() {
        assertThrows(ServiceException.class, () -> articles.create(new ArticleCreateCmd(999L,0L,"标题",null,"https://example.test/a.png",null,0,0,false,false,"正文")));
        assertThrows(ServiceException.class, () -> categories.update(new ArticleCategoryUpdateCmd(category,"公告",null,2,0)));
        assertThrows(ServiceException.class, () -> categories.page(new ArticleCategoryPageQry(null,null,java.time.LocalDateTime.now(),java.time.LocalDateTime.now().minusDays(1),1,20)));
        assertEquals(404, assertThrows(ServiceException.class, () -> articles.increment(999L)).getCode());
        assertEquals(0, articles.page(new ArticlePageQry(null,null,null,null,null,null,null,null,null,1,20)).getTotal());
        assertEquals(0, categories.detail(category).status());
    }
    @Test
    void publicHttpContractsKeepFalseAndSeparateBrowseWrites() throws Exception {
        Long article = articles.create(draft("公开", false));
        var builder = tools.jackson.databind.json.JsonMapper.builder();
        new com.lemnt.common.start.config.JacksonConfig().customizer().customize(builder);
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(
                fixture.components.get(com.lemnt.mall.adapter.app.promotion.article.AppArticleController.class),
                fixture.components.get(com.lemnt.mall.adapter.app.promotion.article.AppArticleCategoryController.class))
                .setControllerAdvice(new com.lemnt.common.security.handler.GlobalExceptionHandler())
                .setMessageConverters(new org.springframework.http.converter.json.JacksonJsonHttpMessageConverter(builder.build())).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/mall/app/articles")
                .param("pageNum", "1").param("pageSize", "20"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.total").value(1));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/mall/app/articles/recommended")
                .param("recommendHot", "false"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data[0].title").value("公开"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/mall/app/articles/detail")
                .param("id", article.toString()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.browseCount").value(0))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.status").doesNotExist());
        assertEquals(0, articles.detail(article).browseCount());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/mall/app/articles/" + article + "/browse-count"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.success").value(true));
        assertEquals(1, articles.detail(article).browseCount());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/mall/app/articles").param("pageSize", "101"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is(422));
    }

}
