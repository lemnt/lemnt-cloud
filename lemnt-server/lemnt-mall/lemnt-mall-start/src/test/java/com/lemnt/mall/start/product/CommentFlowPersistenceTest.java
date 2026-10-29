package com.lemnt.mall.start.product;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.product.cmd.CommentCreateCmd;
import com.lemnt.mall.application.product.cmd.CommentReplyCmd;
import com.lemnt.mall.application.product.cmd.CommentVisibleCmd;
import com.lemnt.mall.application.product.executor.CommentPublicPageQueryExe;
import com.lemnt.mall.application.product.executor.CommentVisibleCmdExe;
import com.lemnt.mall.application.product.qry.CommentPublicPageQry;
import com.lemnt.mall.application.product.service.ProductCommentService;
import com.lemnt.mall.domain.member.gateway.MemberLookupGateway;
import com.lemnt.mall.domain.member.model.MemberSnapshot;
import com.lemnt.mall.domain.product.gateway.ProductCommentGateway;
import com.lemnt.mall.domain.product.model.ProductComment;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 真实生产DDL/Mapper/事务；订单和会员仅替换远程传输，不替换评价存储。 */
class CommentFlowPersistenceTest {
    CatalogPersistenceTest fixture;
    ProductCommentService comments;
    Long spuId;
    Long skuId;
    Map<String, SettlementOrder> orders;
    boolean memberUnavailable;
    Map<String, Long> itemIds;
    long nextItemId;

    @BeforeEach
    void setup() throws Exception {
        fixture = new CatalogPersistenceTest();
        fixture.setup();
        var spu = fixture.create("COMMENT", fixture.draft("红色", 5));
        spuId = spu.id(); skuId = spu.skus().get(0).id();
        orders = new HashMap<>();
        itemIds = new HashMap<>();
        fixture.components.register(OrderGateway.class, new OrderGateway() {
            public SettlementOrder save(SettlementOrder value) { throw new UnsupportedOperationException(); }
            public SettlementOrder findByOrderNo(String no) { return orders.get(no); }
            public Long createComment(com.lemnt.mall.domain.product.model.CommentOrderRequest value) {
                return comments.saveVerified(new com.lemnt.mall.application.product.cmd.CommentSaveCmd(value.memberId(),
                        value.orderNo(), value.skuId(), 10L, itemIds.get(value.orderNo()), spuId, "购买时商品", "https://example.test/old.png",
                        List.of(new com.lemnt.mall.application.product.cmd.CommentSaveCmd.Property(8L,"颜色",9L,"原红色")), true, true,
                        value.score(), value.content(), value.descriptionScores(), value.benefitScores(), value.anonymous(), value.picUrls())).id();
            }
        });
        fixture.components.register(MemberLookupGateway.class, id -> {
            if (memberUnavailable) throw new IllegalStateException("会员RPC不可用");
            return new MemberSnapshot(id, 1, 100, 0L, "真实昵称", "https://example.test/avatar.png");
        });
        comments = fixture.components.get(ProductCommentService.class);
    }

    @AfterEach
    void close() throws Exception { com.lemnt.common.security.context.SecurityContextHolder.remove(); fixture.closeSchemaConnection(); }

    void completed(String no) {
        itemIds.put(no, ++nextItemId);
        orders.put(no, new SettlementOrder(10L, no, 1L, BigDecimal.TEN, "COMPLETED", List.of(
                new SettlementItem(skuId, "SKU", "购买时规格", BigDecimal.TEN, 1, BigDecimal.TEN,
                        BigDecimal.TEN, BigDecimal.ZERO, spuId, "购买时商品", List.of(
                        new SettlementItem.Property(8L, "颜色", 9L, "原红色")), "https://example.test/old.png")), LocalDateTime.now()));
    }

    CommentCreateCmd cmd(String no, int description, int benefit, boolean anonymous) {
        return new CommentCreateCmd(1L, no, skuId, null, "真实评价", description, benefit, anonymous,
                List.of("https://example.test/review.png"));
    }

    @Test
    void savesSourceDimensionsAndOriginalOrderSnapshotAndReplyDoesNotOverwriteIt() throws Exception {
        completed("S1");
        try (var statement = fixture.schemaConnection.createStatement()) {
            statement.execute("UPDATE mall_product_spu SET name='当前已改名' WHERE id=" + spuId);
        }
        var saved = comments.create(cmd("S1", 5, 4, true));
        assertEquals(4, saved.score()); assertEquals(5, saved.descriptionScores()); assertEquals(4, saved.benefitScores());
        assertEquals("真实昵称", saved.userNickname()); assertEquals("购买时商品", saved.spuName());
        assertEquals(List.of(new ProductComment.Property(8L,"颜色",9L,"原红色")),saved.skuProperties());
        assertEquals(10L, saved.orderId()); assertFalse(saved.replyStatus());
        var replied = comments.reply(new CommentReplyCmd(saved.id(), 99L, "谢谢"));
        assertTrue(replied.replyStatus()); assertEquals(saved.skuProperties(), replied.skuProperties());
        assertEquals(saved.picUrls(), replied.picUrls()); assertEquals(saved.anonymous(), replied.anonymous());
        assertEquals(replied, comments.reply(new CommentReplyCmd(saved.id(), 99L, "谢谢")));
    }

    @Test
    void retriesUseSavedFullContentAndDoNotNeedNewMemberOrOrderReads() {
        completed("S2"); var original=cmd("S2",5,4,false); var saved=comments.create(original);
        memberUnavailable=true; orders.clear();
        assertEquals(saved, comments.create(original));
        assertEquals(409,assertThrows(ServiceException.class,()->comments.create(cmd("S2",4,5,false))).getCode());
        assertEquals(409,assertThrows(ServiceException.class,()->comments.create(cmd("S2",5,4,true))).getCode());
        var different = new CommentCreateCmd(1L,"S2",skuId,null,"真实评价",5,4,false,List.of());
        assertEquals(409,assertThrows(ServiceException.class,()->comments.create(different)).getCode());
        assertThrows(ServiceException.class,()->comments.create(new CommentCreateCmd(2L,"S2",skuId,4,"真实评价")));
    }

    @Test
    void publicPageFiltersVisibilityAndRatingGroupsAndAnonymousHttpHidesIdentity() throws Exception {
        for(String no:List.of("G","M","B","H")) completed(no);
        var good=comments.create(cmd("G",5,4,true));comments.create(cmd("M",3,3,false));comments.create(cmd("B",1,2,false));
        var hidden=comments.create(cmd("H",5,5,false));fixture.components.get(CommentVisibleCmdExe.class).execute(new CommentVisibleCmd(hidden.id(),false));
        var query=fixture.components.get(CommentPublicPageQueryExe.class);
        assertEquals(3,query.execute(new CommentPublicPageQry(spuId,0,1,20)).getTotal());
        for(int type=1;type<=3;type++)assertEquals(1,query.execute(new CommentPublicPageQry(spuId,type,1,20)).getTotal());
        var builder=tools.jackson.databind.json.JsonMapper.builder();new com.lemnt.common.start.config.JacksonConfig().customizer().customize(builder);
        var mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(fixture.components.get(com.lemnt.mall.adapter.app.product.AppCommentController.class))
                .setControllerAdvice(new com.lemnt.common.security.handler.GlobalExceptionHandler())
                .setMessageConverters(new org.springframework.http.converter.json.JacksonJsonHttpMessageConverter(builder.build())).build();
        mvc.perform(get("/mall/app/comments").param("spuId",spuId.toString()).param("type","1").param("visible","false").param("memberId","2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.rows[0].userNickname").value("匿名用户"))
                .andExpect(jsonPath("$.rows[0].userAvatar").doesNotExist()).andExpect(jsonPath("$.rows[0].memberId").doesNotExist())
                .andExpect(jsonPath("$.rows[0].orderNo").doesNotExist()).andExpect(jsonPath("$.rows[0].replyUserId").doesNotExist());
        mvc.perform(get("/mall/app/comments")).andExpect(status().is(422));
        mvc.perform(get("/mall/app/comments").param("spuId",spuId.toString())).andExpect(status().is(422));
        assertEquals("真实昵称",fixture.components.get(ProductCommentGateway.class).find(good.id()).userNickname());
        try(var statement=fixture.schemaConnection.createStatement()) { statement.execute("UPDATE mall_product_comment SET deleted=TRUE WHERE id="+good.id()); }
        assertEquals(2,query.execute(new CommentPublicPageQry(spuId,0,1,20)).getTotal());
        assertNull(fixture.components.get(ProductCommentGateway.class).lock(good.id()));
        assertEquals(409,assertThrows(ServiceException.class,()->comments.create(cmd("G",5,4,true))).getCode());
    }

    @Test
    void concurrentIdenticalWritesKeepOneRecordAndDimensionValidationRejectsInvalidInputs() throws Exception {
        completed("S3");var pool=Executors.newFixedThreadPool(4);
        try{
            var tasks=java.util.stream.IntStream.range(0,4).mapToObj(i->pool.submit(()->comments.create(cmd("S3",5,4,false)))).toList();
            var id=tasks.get(0).get(10,TimeUnit.SECONDS).id();
            for(var task:tasks)assertEquals(id,task.get(10,TimeUnit.SECONDS).id());
        }finally{pool.shutdownNow();}
        completed("INVALID");
        assertThrows(ServiceException.class,()->comments.create(new CommentCreateCmd(1L,"INVALID",skuId,5,"内容",5,4,false,List.of())));
        assertThrows(ServiceException.class,()->comments.create(new CommentCreateCmd(1L,"INVALID",skuId,null,"内容",5,null,false,List.of())));
        assertThrows(ServiceException.class,()->comments.create(new CommentCreateCmd(1L,"INVALID",skuId,null,"内容",5,5,false,List.of("javascript:alert(1)"))));
        assertNull(fixture.components.get(ProductCommentGateway.class).findByOrderItem(itemIds.get("INVALID")));
    }

    @Test
    void legacyUpgradeKeepsUnknownSnapshotsNullAndBackfillsOnlyKnownReplyFact() throws Exception {
        var source=new org.h2.jdbcx.JdbcDataSource();source.setURL("jdbc:h2:mem:comment_upgrade_"+UUID.randomUUID()+";MODE=MySQL");
        try(var connection=source.getConnection();var statement=connection.createStatement()){
            statement.execute("CREATE TABLE mall_product_comment(id BIGINT, score INT, reply_content VARCHAR(1000))");
            statement.execute("INSERT INTO mall_product_comment VALUES(1,5,NULL),(2,3,'已回复')");
            ScriptUtils.executeSqlScript(connection,new ClassPathResource("sql/upgrade/product_comment_fields.sql"));
            try(var rows=statement.executeQuery("SELECT description_scores,benefit_scores,anonymous,spu_name,pic_urls,reply_status FROM mall_product_comment ORDER BY id")){
                assertTrue(rows.next());for(int i=1;i<=5;i++)assertNull(rows.getObject(i));assertFalse(rows.getBoolean(6));
                assertTrue(rows.next());assertTrue(rows.getBoolean(6));
            }
        }
    }
    @Test
    void httpImageInputUsesSameRulesAndCannotForgeMemberOrSnapshots() throws Exception {
        completed("HTTP"); completed("BAD-IMAGE");
        com.lemnt.common.security.context.SecurityContextHolder.setUserId("1");
        var builder=tools.jackson.databind.json.JsonMapper.builder();new com.lemnt.common.start.config.JacksonConfig().customizer().customize(builder);
        var mapper=builder.build();
        var mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(fixture.components.get(com.lemnt.mall.adapter.app.product.AppCommentController.class))
                .setControllerAdvice(new com.lemnt.common.security.handler.GlobalExceptionHandler())
                .setMessageConverters(new org.springframework.http.converter.json.JacksonJsonHttpMessageConverter(mapper)).build();
        var body=new HashMap<String,Object>();body.put("orderNo","HTTP");body.put("skuId",skuId);body.put("descriptionScores",5);body.put("benefitScores",4);
        body.put("anonymous",true);body.put("content","来自HTTP的评价");body.put("picUrls",List.of("https://example.test/reviews.png"));
        body.put("memberId",2);body.put("userNickname","伪造昵称");body.put("spuName","伪造商品");
        mvc.perform(post("/mall/app/comments").contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").exists());
        var saved=fixture.components.get(ProductCommentGateway.class).findByOrderItem(itemIds.get("HTTP"));
        assertEquals(1L,saved.memberId());assertEquals("真实昵称",saved.userNickname());assertEquals("购买时商品",saved.spuName());
        assertEquals(List.of("https://example.test/reviews.png"),saved.picUrls());
        body.put("orderNo","BAD-IMAGE");body.put("picUrls",List.of("https://example.test/has space.png"));
        mvc.perform(post("/mall/app/comments").contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body)))
                .andExpect(status().is(422));
        assertNull(fixture.components.get(ProductCommentGateway.class).findByOrderItem(itemIds.get("BAD-IMAGE")));
        completed("RPC-FAIL");memberUnavailable=true;
        assertThrows(IllegalStateException.class,()->comments.create(cmd("RPC-FAIL",5,5,false)));
        assertNull(fixture.components.get(ProductCommentGateway.class).findByOrderItem(itemIds.get("RPC-FAIL")));
    }

}
