package com.lemnt.mall.start.promotion;

import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.security.auth.AuthLogic;
import com.lemnt.common.security.auth.AuthUtil;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.mall.adapter.web.checkout.req.CouponTemplateCreateReq;
import com.lemnt.mall.adapter.web.promotion.PromotionAccess;
import com.lemnt.mall.adapter.web.promotion.PromotionController;
import com.lemnt.mall.adapter.web.promotion.PromotionExceptionHandler;
import com.lemnt.mall.adapter.web.promotion.configuration.PromotionJsonConverter;
import com.lemnt.mall.application.promotion.executor.PromotionCreateCmdExe;
import com.lemnt.mall.application.promotion.executor.PromotionDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.PromotionDraftSaveCmdExe;
import com.lemnt.mall.application.promotion.executor.PromotionPageQueryExe;
import com.lemnt.mall.application.promotion.executor.PromotionPublishCmdExe;
import com.lemnt.mall.application.promotion.executor.PromotionStateChangeCmdExe;
import com.lemnt.mall.application.promotion.service.PromotionManagementService;
import com.lemnt.mall.application.promotion.service.PromotionStoreService;
import com.lemnt.mall.domain.promotion.gateway.PromotionCatalogFactsGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionCounterGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionMemberCounterGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionRequestGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionRootGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionVersionGateway;
import com.lemnt.mall.domain.promotion.model.PricingException;
import com.lemnt.mall.domain.promotion.model.PromotionCatalogUnavailableException;
import com.lemnt.mall.domain.promotion.model.PromotionScope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Real HTTP serialization/auth checks and real trade SQL; scope RPC is an explicit test bridge.
 */
class PromotionHttpIntegrationTest {
    PromotionPersistenceIntegrationTest f;
    MockMvc mvc;
    JsonMapper json;
    AuthLogic previous;
    boolean unavailable, missing;
    int calls;

    @SuppressWarnings("unchecked")
    <T> T proxy(T target) {
        var p = new ProxyFactory(target);
        p.addAdvice(
                new TransactionInterceptor(
                new DataSourceTransactionManager(f.source),
                new AnnotationTransactionAttributeSource()));
        return (T) p.getProxy();
    }

    @BeforeEach
    void setup() throws Exception {
        f = new PromotionPersistenceIntegrationTest();
        f.setup();
        previous = AuthUtil.authLogic;
        AuthUtil.authLogic = new AuthLogic();
        var facts =
        new PromotionCatalogFactsGateway() {
            public List<com.lemnt.mall.domain.promotion.model.PromotionPricingFacts> facts(List<Long> ids) {
                throw new UnsupportedOperationException();
            }

            public void validateScope(PromotionScope scope) {
                if (scope.kind() == PromotionScope.Kind.ALL) {
                    return ;
                }
                calls++;
                if (unavailable) {
                    throw new PromotionCatalogUnavailableException();
                }
                PricingException.require(!missing);
            }
        };
        var service = proxy(new PromotionManagementService(f.components.get(PromotionCounterGateway.class), f.components.get(PromotionMemberCounterGateway.class), f.components.get(PromotionRequestGateway.class), f.components.get(PromotionRootGateway.class), f.components.get(PromotionStoreService.class), f.components.get(PromotionVersionGateway.class), facts));
        var controller =
        new PromotionController(
                new PromotionAccess(),
                new PromotionCreateCmdExe(service),
                new PromotionDraftSaveCmdExe(service),
                new PromotionPublishCmdExe(service),
                new PromotionStateChangeCmdExe(service),
                proxy(new PromotionDetailQueryExe(f.components.get(PromotionRootGateway.class), f.components.get(PromotionVersionGateway.class))),
                proxy(new PromotionPageQueryExe(f.components.get(PromotionRootGateway.class))));
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        json = builder.build();
        mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new PromotionExceptionHandler())
            .setMessageConverters(
                new PromotionJsonConverter(),
                new JacksonJsonHttpMessageConverter(json))
            .build();
        user(7, Set.of("mall:promotion:*"));
    }

    @AfterEach
    void cleanup() throws Exception {
        SecurityContextHolder.remove();
        AuthUtil.authLogic = previous;
        f.cleanup();
    }

    void user(long id, Set<String> perms) {
        SecurityContextHolder.remove();
        SecurityContextHolder.set(
                SecurityConstants.LOGIN_USER,
                new SecurityUser()
                .setUserId(id)
                .setPermissions(perms)
                .setRoles(Set.of())
                .setExpireTime(System.currentTimeMillis() + 3600000));
    }

    String body(String request, String code) {
        return """
        {"requestNo":"%s","code":"%s","draft":{"name":"促销","type":"FULL_REDUCTION","scope":{"kind":"SKU_IDS","ids":["1","2"]},"audience":"ALL_AUTHENTICATED","couponPolicy":"ALLOW_ONE","priority":10,"startsAtEpochMs":"%d","endsAtEpochMs":"%d","globalUseLimit":null,"memberUseLimit":null,"rule":{"tiers":[{"thresholdFen":"100","discountFen":"10"}]}}}
        """
            .formatted(request, code, f.now - 10000, f.now + 3600000);
    }

    ResultActions create(String content) throws Exception {
        return mvc.perform(
                post("/mall/admin/promotions")
                .header("Authorization", "Bearer test-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
    }

    long created(String request, String code) throws Exception {
        var result = create(body(request, code)).andExpect(status().isOk()).andReturn();
        return Long.parseLong(
                json.readTree(result.getResponse().getContentAsString())
                .get("data")
                .get("id")
                .asString());
    }

    ResultActions publish(long id, String request, int revision) throws Exception {
        return mvc.perform(
                post("/mall/admin/promotions/" + id + "/publish")
                .header("Authorization", "Bearer test-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                "{\"requestNo\":\""
                + request
                + "\",\"expectedRevision\":"
                + revision
                + "}"));
    }

    ResultActions state(long id, String request, int rev, String action) throws Exception {
        return mvc.perform(
                put("/mall/admin/promotions/" + id + "/state")
                .header("Authorization", "Bearer test-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                "{\"requestNo\":\""
                + request
                + "\",\"expectedRevision\":"
                + rev
                + ",\"action\":\""
                + action
                + "\"}"));
    }

    long count(String table) {
        return f.jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }

    @Test
    void createWireContractStartsAtZeroAndUsesActualUserInsteadOfHeader() throws Exception {
        SecurityContextHolder.setUserId("999");
        var result =
        create(body("CREATE", "CODE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.revision").value(0))
            .andExpect(jsonPath("$.data.state").value("DRAFT"))
            .andReturn();
        var data = json.readTree(result.getResponse().getContentAsString()).get("data");
        assertTrue(data.get("id").isString());
        assertTrue(data.get("revision").isIntegralNumber());
        assertTrue(data.get("activeVersion").isNull());
        assertEquals(
                Set.of("id", "revision", "state", "activeVersion"),
                new HashSet<>(data.propertyNames()));
        assertEquals(
                7,
                f.jdbc.queryForObject(
                "SELECT operator_id FROM mall_promotion_admin_request", Long.class));
        assertEquals(1, calls);
    }

    @Test
    void allSixRoutesUseRealWriteQueryAndImmutableVersions() throws Exception {
        long id = created("CREATE", "CODE");
        String draft = json.readTree(body("X", "X")).get("draft").toString();
        mvc.perform(
                put("/mall/admin/promotions/" + id + "/draft")
                .header("Authorization", "Bearer test-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                "{\"requestNo\":\"SAVE\",\"expectedRevision\":0,\"draft\":"
                + draft
                + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.revision").value(1));
        publish(id, "PUB", 1)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.activeVersion").value(1));
        state(id, "PAUSE", 2, "PAUSE").andExpect(status().isOk());
        state(id, "RESUME", 3, "RESUME").andExpect(status().isOk());
        state(id, "ARCHIVE", 4, "ARCHIVE").andExpect(status().isOk());
        mvc.perform(
                get("/mall/admin/promotions/" + id)
                .header("Authorization", "Bearer test-admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.state").value("ARCHIVED"))
            .andExpect(jsonPath("$.data.draft.rule.tiers[0].discountFen").value("10"))
            .andExpect(jsonPath("$.data.activeRule.scope.ids[0]").value("1"))
            .andExpect(jsonPath("$.data.timePhase").value("RUNNING"));
        mvc.perform(
                get("/mall/admin/promotions")
                .param("state", "ARCHIVED")
                .header("Authorization", "Bearer test-admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.rows[0].revision").value(5));
        assertEquals(1, count("mall_promotion_version"));
    }

    @Test
    void replaySkipsRpcAndRevisionButPermissionsStillApply() throws Exception {
        long id = created("CREATE", "CODE");
        publish(id, "PUB", 0).andExpect(status().isOk());
        state(id, "PAUSE", 1, "PAUSE").andExpect(status().isOk());
        int initial = calls;
        unavailable = true;
        publish(id, "PUB", 0)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.revision").value(1))
            .andExpect(jsonPath("$.data.state").value("PUBLISHED"));
        create(body("CREATE", "CODE")).andExpect(status().isOk());
        assertEquals(initial, calls);
        user(7, Set.of("mall:promotion:query"));
        publish(id, "PUB", 0)
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403));
        assertEquals(initial, calls);
    }

    @Test
    void missingTargetsAndRpcFailureRollBackDraftAndRequest() throws Exception {
        missing = true;
        create(body("R1", "CODE")).andExpect(status().isBadRequest());
        assertEquals(0, count("mall_promotion_root"));
        assertEquals(0, count("mall_promotion_admin_request"));
        missing = false;
        unavailable = true;
        create(body("R1", "CODE"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.data.errorCode").value("CATALOG_FACTS_UNAVAILABLE"))
            .andExpect(jsonPath("$.data.retryable").value(true));
        assertEquals(0, count("mall_promotion_admin_request"));
        unavailable = false;
        created("R1", "CODE");
    }

    @Test
    void publishRechecksScopeAndFailureLeavesNoVersionOrReceipt() throws Exception {
        long id = created("CREATE", "CODE");
        missing = true;
        publish(id, "PUB", 0).andExpect(status().isBadRequest());
        assertEquals(0, count("mall_promotion_version"));
        assertEquals(1, count("mall_promotion_admin_request"));
        missing = false;
        publish(id, "PUB", 0).andExpect(status().isOk());
    }

    @Test
    void strictUnknownDuplicateTrailingAndWrongRuleFieldsRejectBeforeWrites() throws Exception {
        String valid = body("R1", "CODE");
        for (String bad :
                List.of(
                valid.replace("\"code\":\"CODE\"", "\"code\":\"CODE\",\"operatorId\":999"),
                valid.replace("\"name\":\"促销\"", "\"name\":\"促销\",\"unknown\":true"),
                valid.replace("\"tiers\":[", "\"thresholdFen\":null,\"tiers\":["),
                valid.replace(
                "\"requestNo\":\"R1\"", "\"requestNo\":\"X\",\"requestNo\":\"R1\""),
                valid + " {}",
                valid.replace("\"globalUseLimit\":null,", ""))) {
            create(bad).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        }
        assertEquals(0, count("mall_promotion_root"));
        assertEquals(0, calls);
    }

    @Test
    void scalarCoercionAndInvalidAmountsScopeWindowsReject() throws Exception {
        String valid = body("R1", "CODE");
        for (String bad :
                List.of(
                valid.replace("\"priority\":10", "\"priority\":\"10\""),
                valid.replace("\"priority\":10", "\"priority\":10.0"),
                valid.replace("\"thresholdFen\":\"100\"", "\"thresholdFen\":100"),
                valid.replace(
                "\"discountFen\":\"10\"",
                "\"discountFen\":\"9223372036854775808\""),
                valid.replace("\"1\",\"2\"", "\"1\",\"1\""),
                valid.replace("ALL_AUTHENTICATED", "VIP"),
                valid.replace("\"memberUseLimit\":null", "\"memberUseLimit\":\"2\"")
                .replace("\"globalUseLimit\":null", "\"globalUseLimit\":\"1\""),
                valid.replace(
                Long.toString(f.now + 3600000), Long.toString(f.now - 20000)))) {
            create(bad).andExpect(status().isBadRequest());
        }
        assertEquals(0, count("mall_promotion_root"));
        assertEquals(0, calls);
    }

    @Test
    void noAdminSessionOrExpiredSessionCannotUseCallerIdentity() throws Exception {
        SecurityContextHolder.remove();
        SecurityContextHolder.setUserId("7");
        create(body("R1", "CODE"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401));
        user(7, Set.of("mall:promotion:*"));
        ((SecurityUser) SecurityContextHolder.get(SecurityConstants.LOGIN_USER, SecurityUser.class))
            .setExpireTime(1L);
        create(body("R1", "CODE")).andExpect(status().isUnauthorized());
        assertEquals(0, count("mall_promotion_root"));
    }

    @Test
    void readPermissionDoesNotGrantAnyMutationOrList() throws Exception {
        long id = created("CREATE", "CODE");
        user(7, Set.of("mall:promotion:query"));
        mvc.perform(
                get("/mall/admin/promotions/" + id)
                .header("Authorization", "Bearer test-admin"))
            .andExpect(status().isOk());
        mvc.perform(get("/mall/admin/promotions").header("Authorization", "Bearer test-admin"))
            .andExpect(status().isForbidden());
        create(body("OTHER", "OTHER")).andExpect(status().isForbidden());
        publish(id, "PUB", 0).andExpect(status().isForbidden());
        state(id, "PAUSE", 0, "PAUSE").andExpect(status().isForbidden());
        assertEquals(1, count("mall_promotion_admin_request"));
    }

    @Test
    void conflictsMissingAndMalformedQueryUseMatchingHttpAndRCode() throws Exception {
        long id = created("CREATE", "CODE");
        create(body("OTHER", "CODE"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.data.errorCode").value("PROMOTION_CODE_CONFLICT"));
        create(body("CREATE", "OTHER"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.data.errorCode").value("ADMIN_REQUEST_CONFLICT"));
        publish(id, "STALE", 1).andExpect(status().isConflict());
        mvc.perform(get("/mall/admin/promotions/999").header("Authorization", "Bearer test-admin"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404));
        for (String query :
                List.of(
                "pageSize=101",
                "pageNum=0",
                "unknown=1",
                "state=UNKNOWN",
                "pageNum=1&pageNum=2")) {
            mvc.perform(
                    get("/mall/admin/promotions?" + query)
                    .header("Authorization", "Bearer test-admin"))
                .andExpect(status().isBadRequest());
        }
    }

    @Test
    void pagedFiltersAreExactBoundedAndDeterministic() throws Exception {
        long a = created("A", "A");
        created("B", "B");
        long c = created("C", "C");
        mvc.perform(
                get("/mall/admin/promotions?pageSize=2")
                .header("Authorization", "Bearer test-admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(3))
            .andExpect(jsonPath("$.rows[0].id").value(Long.toString(c)));
        mvc.perform(
                get("/mall/admin/promotions?pageSize=2&pageNum=2")
                .header("Authorization", "Bearer test-admin"))
            .andExpect(jsonPath("$.rows[0].id").value(Long.toString(a)));
        mvc.perform(
                get("/mall/admin/promotions?code=B")
                .header("Authorization", "Bearer test-admin"))
            .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void scopedConverterAndPermissionsDeclaredAndBodyBoundEnforced() throws Exception {
        var strict = new PromotionJsonConverter();
        assertFalse(
                strict.canRead(
                CouponTemplateCreateReq.class,
                MediaType.APPLICATION_JSON));
        for (var method : PromotionController.class.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                assertNotNull(
                        method.getAnnotation(
                        RequiresPermissions.class));
            }
        }
        create(body("R1", "CODE").replace("促销", "x".repeat(1048576)))
            .andExpect(status().isBadRequest());
        assertEquals(0, count("mall_promotion_root"));
    }

    @Test
    void orderDiscountRoundTripsNullableCapAndAllScopeSkipsUnavailableRpc() throws Exception {
        unavailable = true;
        String value =
        body("PERCENT", "PERCENT")
            .replace("FULL_REDUCTION", "ORDER_DISCOUNT")
            .replace(
                "\"kind\":\"SKU_IDS\",\"ids\":[\"1\",\"2\"]",
                "\"kind\":\"ALL\",\"ids\":[]")
            .replace(
                "\"rule\":{\"tiers\":[{\"thresholdFen\":\"100\",\"discountFen\":\"10\"}]}",
                "\"rule\":{\"thresholdFen\":\"0\",\"payRateBps\":8000,\"maxDiscountFen\":null}");
        var result = create(value).andExpect(status().isOk()).andReturn();
        long id =
        Long.parseLong(
                json.readTree(result.getResponse().getContentAsString())
                .get("data")
                .get("id")
                .asString());
        publish(id, "PUB", 0).andExpect(status().isOk());
        var detail =
        mvc.perform(
                get("/mall/admin/promotions/" + id)
                .header("Authorization", "Bearer test-admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.activeRule.rule.payRateBps").value(8000))
            .andReturn();
        var rule =
        json.readTree(detail.getResponse().getContentAsString())
            .get("data")
            .get("activeRule")
            .get("rule");
        assertTrue(rule.get("maxDiscountFen").isNull());
        assertFalse(rule.has("tiers"));
        assertEquals(0, calls);
        create(value.replace("PERCENT", "BAD").replace(",\"maxDiscountFen\":null", ""))
            .andExpect(status().isBadRequest());
    }

    @Test
    void exhaustedRevisionFailsWithoutPublishingPartialVersion() throws Exception {
        long id = created("CREATE", "CODE");
        f.jdbc.update(
                "UPDATE mall_promotion_root SET revision=? WHERE id=?", Integer.MAX_VALUE, id);
        publish(id, "EXHAUSTED", Integer.MAX_VALUE)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.data.errorCode").value("PROMOTION_REVISION_CONFLICT"));
        assertEquals(0, count("mall_promotion_version"));
        assertEquals(1, count("mall_promotion_admin_request"));
    }
}
