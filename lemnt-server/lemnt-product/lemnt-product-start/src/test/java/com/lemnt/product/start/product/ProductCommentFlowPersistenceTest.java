package com.lemnt.product.start.product;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.product.adapter.app.product.AppProductCommentController;
import com.lemnt.product.adapter.web.product.ProductCommentController;
import com.lemnt.product.application.product.cmd.ProductCommentCreateCmd;
import com.lemnt.product.application.product.cmd.ProductCommentReplyCmd;
import com.lemnt.product.application.product.cmd.ProductCommentSaveCmd;
import com.lemnt.product.application.product.cmd.ProductCommentSelfCreateCmd;
import com.lemnt.product.application.product.cmd.ProductCommentVisibleCmd;
import com.lemnt.product.application.product.executor.ProductCommentPageQueryExe;
import com.lemnt.product.application.product.executor.ProductCommentPublicPageQueryExe;
import com.lemnt.product.application.product.executor.ProductCommentSelfCreateCmdExe;
import com.lemnt.product.application.product.executor.ProductCommentVisibleCmdExe;
import com.lemnt.product.application.product.qry.ProductCommentPageQry;
import com.lemnt.product.application.product.qry.ProductCommentPublicPageQry;
import com.lemnt.product.application.product.service.ProductCommentService;
import com.lemnt.product.domain.product.gateway.ProductCommentGateway;
import com.lemnt.product.domain.product.model.ProductComment;

import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 真实生产DDL/Mapper/事务；订单和会员仅替换远程传输，不替换评价存储。
 */
class ProductCommentFlowPersistenceTest {
    CatalogPersistenceTest fixture;
    ProductCommentService comments;
    Long spuId;
    Long skuId;
    Map<String, Long> itemIds;
    long nextItemId;
    // 已验证评价快照：生产由 order 从登录上下文填入并随 SaveCmd 传入，测试直接提供
    static final String NICK = "真实昵称";
    static final String AVATAR = "https://example.test/avatar.png";

    @BeforeEach
    void setup() throws Exception {
        fixture = new CatalogPersistenceTest();
        fixture.setup();
        var spu = fixture.create("COMMENT", fixture.draft("红色", 5));
        spuId = spu.id();
        skuId = spu.skus().get(0).id();
        itemIds = new HashMap<>();
        comments = fixture.components.get(ProductCommentService.class);
    }

    @AfterEach
    void close() throws Exception {
        SecurityContextHolder.remove();
        fixture.closeSchemaConnection();
    }

    void completed(String no) {
        itemIds.put(no, ++nextItemId);
    }

    /** 投稿入口迁往 order 后，产品侧写入口即已验证事实保存。 */
    ProductComment submit(ProductCommentCreateCmd input) {
        return comments.saveVerified(
            verified(input, itemIds.get(input.orderNo()), true, true));
    }

    ProductCommentCreateCmd cmd(String no, int description, int benefit, boolean anonymous) {
        return new ProductCommentCreateCmd(
            1L,
            no,
            skuId,
            null,
            "真实评价",
            description,
            benefit,
            anonymous,
            List.of("https://example.test/review.png"));
    }

    @Test
    void savesSourceDimensionsAndOriginalOrderSnapshotAndReplyDoesNotOverwriteIt()
        throws Exception {
        completed("S1");
        try (var statement = fixture.schemaConnection.createStatement()) {
            statement.execute("UPDATE product_spu SET name='当前已改名' WHERE id=" + spuId);
        }
        var saved = submit(cmd("S1", 5, 4, true));
        assertEquals(4, saved.score());
        assertEquals(5, saved.descriptionScores());
        assertEquals(4, saved.benefitScores());
        assertEquals("真实昵称", saved.userNickname());
        assertEquals("购买时商品", saved.spuName());
        assertEquals(List.of(new ProductComment.Spec(8L, "颜色", 9L, "原红色")), saved.skuProperties());
        assertEquals(10L, saved.orderId());
        assertFalse(saved.replyStatus());
        var replied = comments.reply(new ProductCommentReplyCmd(saved.id(), 99L, "谢谢"));
        assertTrue(replied.replyStatus());
        assertEquals(saved.skuProperties(), replied.skuProperties());
        assertEquals(saved.picUrls(), replied.picUrls());
        assertEquals(saved.anonymous(), replied.anonymous());
        assertEquals(replied, comments.reply(new ProductCommentReplyCmd(saved.id(), 99L, "谢谢")));
    }

    @Test
    void retriesUseSavedFullContentAndDoNotNeedNewMemberReads() {
        completed("S2");
        var original = cmd("S2", 5, 4, false);
        var saved = submit(original);
        assertEquals(saved, submit(original));
        assertEquals(
            409,
            assertThrows(ServiceException.class, () -> submit(cmd("S2", 4, 5, false)))
                .getCode());
        assertEquals(
            409,
            assertThrows(ServiceException.class, () -> submit(cmd("S2", 5, 4, true)))
                .getCode());
        var different = new ProductCommentCreateCmd(1L, "S2", skuId, null, "真实评价", 5, 4, false, List.of());
        assertEquals(
            409,
            assertThrows(ServiceException.class, () -> submit(different)).getCode());
        assertThrows(
            ServiceException.class,
            () -> submit(new ProductCommentCreateCmd(2L, "S2", skuId, 4, "真实评价")));
    }

    @Test
    void publicPageFiltersVisibilityAndRatingGroupsAndAnonymousHttpHidesIdentity()
        throws Exception {
        for (String no : List.of("G", "M", "B", "H")) completed(no);
        var good = submit(cmd("G", 5, 4, true));
        submit(cmd("M", 3, 3, false));
        submit(cmd("B", 1, 2, false));
        var hidden = submit(cmd("H", 5, 5, false));
        fixture.components
            .get(ProductCommentVisibleCmdExe.class)
            .execute(new ProductCommentVisibleCmd(hidden.id(), false));
        var query = fixture.components.get(ProductCommentPublicPageQueryExe.class);
        assertEquals(3, query.execute(new ProductCommentPublicPageQry(spuId, 0, 1, 20)).getTotal());
        for (int type = 1; type <= 3; type++)
            assertEquals(1, query.execute(new ProductCommentPublicPageQry(spuId, type, 1, 20)).getTotal());
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        var mvc =
            MockMvcBuilders.standaloneSetup(fixture.components.get(AppProductCommentController.class))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(builder.build()))
                .build();
        mvc.perform(
                get("/app/comments")
                    .param("spuId", spuId.toString())
                    .param("type", "1")
                    .param("visible", "false")
                    .param("memberId", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.rows[0].userNickname").value("匿名用户"))
            .andExpect(jsonPath("$.rows[0].userAvatar").doesNotExist())
            .andExpect(jsonPath("$.rows[0].memberId").doesNotExist())
            .andExpect(jsonPath("$.rows[0].orderNo").doesNotExist())
            .andExpect(jsonPath("$.rows[0].replyUserId").doesNotExist());
        mvc.perform(get("/app/comments")).andExpect(status().is(422));
        mvc.perform(get("/app/comments").param("spuId", spuId.toString()))
            .andExpect(status().is(422));
        assertEquals(
            "真实昵称",
            fixture.components.get(ProductCommentGateway.class).find(good.id()).userNickname());
        try (var statement = fixture.schemaConnection.createStatement()) {
            statement.execute("UPDATE product_comment SET deleted=TRUE WHERE id=" + good.id());
        }
        assertEquals(2, query.execute(new ProductCommentPublicPageQry(spuId, 0, 1, 20)).getTotal());
        assertNull(fixture.components.get(ProductCommentGateway.class).lock(good.id()));
        assertEquals(
            409,
            assertThrows(ServiceException.class, () -> submit(cmd("G", 5, 4, true)))
                .getCode());
    }

    @Test
    void concurrentIdenticalWritesKeepOneRecordAndDimensionValidationRejectsInvalidInputs()
        throws Exception {
        completed("S3");
        var pool = Executors.newFixedThreadPool(4);
        try {
            var tasks =
                IntStream.range(0, 4)
                    .mapToObj(
                        i -> pool.submit(() -> submit(cmd("S3", 5, 4, false))))
                    .toList();
            var id = tasks.get(0).get(10, TimeUnit.SECONDS).id();
            for (var task : tasks) assertEquals(id, task.get(10, TimeUnit.SECONDS).id());
        } finally {
            pool.shutdownNow();
        }
        completed("INVALID");
        assertThrows(
            ServiceException.class,
            () ->
                submit(
                    new ProductCommentCreateCmd(
                        1L, "INVALID", skuId, 5, "内容", 5, 4, false, List.of())));
        assertThrows(
            ServiceException.class,
            () ->
                submit(
                    new ProductCommentCreateCmd(
                        1L, "INVALID", skuId, null, "内容", 5, null, false,
                        List.of())));
        assertThrows(
            ServiceException.class,
            () ->
                submit(
                    new ProductCommentCreateCmd(
                        1L,
                        "INVALID",
                        skuId,
                        null,
                        "内容",
                        5,
                        5,
                        false,
                        List.of("javascript:alert(1)"))));
        assertNull(
            fixture.components
                .get(ProductCommentGateway.class)
                .findByOrderItem(itemIds.get("INVALID")));
    }

    @Test
    void initializationKeepsHistoricalUnknownSnapshotsAndKnownReplyFact() throws Exception {
        try (var statement = fixture.schemaConnection.createStatement()) {
            statement.execute(
                "INSERT INTO product_comment(id,spu_id,sku_id,member_id,score,content,reply_content,reply_status) VALUES(900,"
                    + spuId
                    + ","
                    + skuId
                    + ",1,5,'旧评价',NULL,FALSE),(901,"
                    + spuId
                    + ","
                    + skuId
                    + ",1,3,'旧评价','已回复',TRUE)");
            ScriptUtils.executeSqlScript(
                fixture.schemaConnection,
                new ClassPathResource("product-sql/lemnt_product.sql"));
            try (var rows =
                     statement.executeQuery(
                         "SELECT description_scores,benefit_scores,anonymous,spu_name,pic_urls,reply_status FROM product_comment ORDER BY id")) {
                assertTrue(rows.next());
                for (int column = 1; column <= 5; column++) assertNull(rows.getObject(column));
                assertFalse(rows.getBoolean(6));
                assertTrue(rows.next());
                assertTrue(rows.getBoolean(6));
                assertFalse(rows.next());
            }
        }
    }

    @Test
    void imageRulesApplyThroughSaveVerifiedAndSnapshotComesFromVerifiedFact() throws Exception {
        completed("BAD-IMAGE");
        var input =
            new ProductCommentCreateCmd(
                1L, "BAD-IMAGE", skuId, 5, "内容", 5, 4, false,
                List.of("https://example.test/has space.png"));
        assertThrows(
            ServiceException.class, () -> comments.saveVerified(verified(input, itemIds.get("BAD-IMAGE"), true, true)));
        assertNull(
            fixture.components
                .get(ProductCommentGateway.class)
                .findByOrderItem(itemIds.get("BAD-IMAGE")));
        completed("HTTP");
        var saved = submit(cmd("HTTP", 5, 4, true));
        // 昵称/头像是order核验后经SaveCmd传入的可信快照，商品改名不影响已存事实
        try (var statement = fixture.schemaConnection.createStatement()) {
            statement.execute("UPDATE product_spu SET name='当前已改名' WHERE id=" + spuId);
        }
        assertEquals(1L, saved.memberId());
        assertEquals("真实昵称", saved.userNickname());
        assertEquals("购买时商品", saved.spuName());
        assertEquals(List.of("https://example.test/review.png"), saved.picUrls());
    }

    private ProductCommentSaveCmd verified(
        ProductCommentCreateCmd input, Long itemId, boolean unique, boolean allowCreate) {
        return new ProductCommentSaveCmd(
            input.memberId(),
            input.orderNo(),
            input.skuId(),
            10L,
            itemId,
            spuId,
            "购买时商品",
            "https://example.test/old.png",
            List.of(new ProductCommentSaveCmd.Spec(8L, "颜色", 9L, "原红色")),
            unique,
            allowCreate,
            input.score(),
            input.content(),
            input.descriptionScores(),
            input.benefitScores(),
            input.anonymous(),
            input.picUrls(),
            NICK,
            AVATAR);
    }

    @Test
    void verifiedLegacyLinkPreservesUnknownHistoryAndRefusesAmbiguousOrDeletedLegacyRows()
        throws Exception {
        completed("OLD");
        completed("AMBIG");
        completed("DELETED");
        var gateway = fixture.components.get(ProductCommentGateway.class);
        var old =
            gateway.save(
                new ProductComment(
                    null, spuId, skuId, "OLD", 1L, 5, "旧评价", true, null, null, null,
                    null));
        var input = new ProductCommentCreateCmd(1L, "OLD", skuId, 5, "旧评价");
        var bound = comments.saveVerified(verified(input, itemIds.get("OLD"), true, true));
        assertEquals(old.id(), bound.id());
        assertEquals(itemIds.get("OLD"), bound.orderItemId());
        assertEquals(10L, bound.orderId());
        assertNull(bound.descriptionScores());
        assertNull(bound.userNickname());
        assertNull(bound.skuProperties());
        assertEquals(
            bound, comments.saveVerified(verified(input, itemIds.get("OLD"), true, false)));
        var ambiguous =
            gateway.save(
                new ProductComment(
                    null, spuId, skuId, "AMBIG", 1L, 5, "旧评价", true, null, null, null,
                    null));
        assertEquals(
            409,
            assertThrows(
                ServiceException.class,
                () ->
                    comments.saveVerified(
                        verified(
                            new ProductCommentCreateCmd(
                                1L, "AMBIG", skuId, 5, "旧评价"),
                            itemIds.get("AMBIG"),
                            false,
                            true)))
                .getCode());
        assertNull(gateway.find(ambiguous.id()).orderItemId());
        var deleted =
            gateway.save(
                new ProductComment(
                    null, spuId, skuId, "DELETED", 1L, 5, "旧评价", true, null, null, null,
                    null));
        try (var statement = fixture.schemaConnection.createStatement()) {
            statement.execute("UPDATE product_comment SET deleted=TRUE WHERE id=" + deleted.id());
        }
        assertEquals(
            409,
            assertThrows(
                ServiceException.class,
                () ->
                    comments.saveVerified(
                        verified(
                            new ProductCommentCreateCmd(
                                1L, "DELETED", skuId, 5, "旧评价"),
                            itemIds.get("DELETED"),
                            true,
                            true)))
                .getCode());
    }

    @Test
    void privateStorageIsUniqueByActualItemRatherThanSkuAndCannotCreateForAnAlreadyCommentedItem() {
        var first =
            comments.saveVerified(
                verified(
                    new ProductCommentCreateCmd(1L, "SAME-SKU", skuId, 5, "第一行"),
                    1001L,
                    false,
                    true));
        var second =
            comments.saveVerified(
                verified(
                    new ProductCommentCreateCmd(1L, "SAME-SKU", skuId, 4, "第二行"),
                    1002L,
                    false,
                    true));
        assertNotEquals(first.id(), second.id());
        assertEquals(1001L, first.orderItemId());
        assertEquals(1002L, second.orderItemId());
        assertEquals(
            first,
            comments.saveVerified(
                verified(
                    new ProductCommentCreateCmd(1L, "SAME-SKU", skuId, 5, "第一行"),
                    1001L,
                    false,
                    false)));
        assertEquals(
            409,
            assertThrows(
                ServiceException.class,
                () ->
                    comments.saveVerified(
                        verified(
                            new ProductCommentCreateCmd(
                                1L, "SAME-SKU", skuId, 5, "不应创建"),
                            1003L,
                            false,
                            false)))
                .getCode());
    }

    @Test
    void adminFiltersUseStoredSnapshotsReplyFlagAndInclusiveTimeBoundaries() throws Exception {
        completed("FILTER");
        var saved = submit(cmd("FILTER", 5, 4, false));
        try (var statement = fixture.schemaConnection.createStatement()) {
            statement.execute(
                "UPDATE product_comment SET create_time='2026-10-29 10:00:00', reply_content='旧正文', reply_status=false WHERE id="
                    + saved.id());
        }
        var query = fixture.components.get(ProductCommentPageQueryExe.class);
        var time = LocalDateTime.of(2026, 10, 29, 10, 0);
        var filter =
            new ProductCommentPageQry(
                spuId, 1L, 4, true, false, 1, 20, "  实昵称  ", 10L, "买时商", time, time);
        assertEquals(1, query.execute(filter).getTotal());
        assertEquals(
            0,
            query.execute(new ProductCommentPageQry(null, null, null, null, true, 1, 20)).getTotal());
        assertEquals(
            0,
            query.execute(
                    new ProductCommentPageQry(
                        null, null, null, null, null, 1, 20, null, 11L, null, null,
                        null))
                .getTotal());
        assertEquals(
            0,
            query.execute(
                    new ProductCommentPageQry(
                        null,
                        null,
                        null,
                        null,
                        null,
                        1,
                        20,
                        null,
                        null,
                        null,
                        time.plusSeconds(1),
                        null))
                .getTotal());
        assertThrows(
            ServiceException.class,
            () ->
                query.execute(
                    new ProductCommentPageQry(
                        null,
                        null,
                        null,
                        null,
                        null,
                        1,
                        20,
                        null,
                        null,
                        null,
                        time.plusSeconds(1),
                        time)));
        assertEquals(
            1,
            query.execute(new ProductCommentPageQry(null, null, null, null, null, 1, 20)).getTotal());
        try (var statement = fixture.schemaConnection.createStatement()) {
            statement.execute("UPDATE product_comment SET deleted=true WHERE id=" + saved.id());
        }
        assertEquals(
            0,
            query.execute(new ProductCommentPageQry(null, null, null, null, null, 1, 20)).getTotal());
    }

    @Test
    void adminHttpBindsIndependentFilterFieldsAndRejectsInvalidRanges() throws Exception {
        completed("FILTER-HTTP");
        submit(cmd("FILTER-HTTP", 5, 4, false));
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        var mvc =
            MockMvcBuilders.standaloneSetup(fixture.components.get(ProductCommentController.class))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(builder.build()))
                .build();
        // 仅验证HTTP绑定和业务响应，standalone不代表网关或权限切面的部署验收。
        mvc.perform(
                get("/comments")
                    .param("userNickname", "真实")
                    .param("spuName", "购买时")
                    .param("orderId", "10")
                    .param("replied", "false")
                    .param("createTimeStart", "2026-01-01T00:00:00")
                    .param("createTimeEnd", "2027-01-01T00:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(1));
        mvc.perform(get("/comments").param("orderId", "-1")).andExpect(status().is(422));
        mvc.perform(get("/comments").param("createTimeStart", "invalid"))
            .andExpect(status().is(422));
        mvc.perform(
                get("/comments")
                    .param("createTimeStart", "2027-01-01T00:00:00")
                    .param("createTimeEnd", "2026-01-01T00:00:00"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/comments").param("userNickname", "字".repeat(256)))
            .andExpect(status().is(422));
    }

    @Test
    void selfCommentUsesSourceZeroMemberAndNoOrderAndRealProductSnapshot() {
        var executor = fixture.components.get(ProductCommentSelfCreateCmdExe.class);
        var input =
            new ProductCommentSelfCreateCmd(
                "自评昵称", "https://example.test/a.png", skuId, 5, 4, "后台自评", List.of());
        var id = executor.execute(input);
        var row = fixture.components.get(ProductCommentGateway.class).find(id);
        assertEquals(0L, row.memberId());
        assertNull(row.orderNo());
        assertNull(row.orderId());
        assertNull(row.orderItemId());
        assertEquals(4, row.score());
        assertEquals(spuId, row.spuId());
        assertTrue(row.visible());
        assertFalse(row.anonymous());
        assertNotNull(row.spuName());
        assertFalse(row.replyStatus());
        assertNotEquals(id, executor.execute(input)); // 明确的新创建，不伪称内容幂等。
        assertThrows(
            ServiceException.class,
            () ->
                executor.execute(
                    new ProductCommentSelfCreateCmd(
                        "昵称",
                        "https://example.test/a.png",
                        Long.MAX_VALUE,
                        5,
                        5,
                        "自评",
                        List.of())));
        assertThrows(
            ServiceException.class,
            () ->
                executor.execute(
                    new ProductCommentSelfCreateCmd(
                        "昵称",
                        "http://example.test/a.png",
                        skuId,
                        5,
                        5,
                        "自评",
                        List.of())));
    }
}
