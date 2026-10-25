package com.lemnt.trade.start.promotion;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.lemnt.trade.application.promotion.service.*;
import com.lemnt.trade.domain.promotion.gateway.PromotionGateway;
import com.lemnt.trade.domain.promotion.model.*;
import com.lemnt.trade.infrastructure.promotion.gateway.PromotionGatewayImpl;
import com.lemnt.trade.infrastructure.promotion.mapper.PromotionMapper;
import com.lemnt.trade.infrastructure.promotion.converter.PromotionConverter;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
import static com.lemnt.trade.domain.promotion.model.PromotionPersistenceException.Code.*;

/** Actual production SQL/Gateway/services/proxies. Not target MySQL, RPC or provider evidence. */
class PromotionPersistenceIntegrationTest {
    JdbcDataSource source;
    Connection schema;
    JdbcTemplate jdbc;
    TransactionTemplate transaction;
    PromotionGateway gateway;
    PromotionMapper mapper;
    PromotionAdministrationService admin;
    PromotionUsageService usage;
    long now;
    int requests;

    @SuppressWarnings("unchecked")
    private <T> T proxy(T target) {
        var p = new ProxyFactory(target);
        p.addAdvice(
                new TransactionInterceptor(
                        new DataSourceTransactionManager(source),
                        new AnnotationTransactionAttributeSource()));
        return (T) p.getProxy();
    }

    private <T> T tx(Supplier<T> action) {
        return transaction.execute(s -> action.get());
    }

    @BeforeEach
    void setup() throws Exception {
        source = new JdbcDataSource();
        source.setURL(
                "jdbc:h2:mem:promotion_"
                        + UUID.randomUUID()
                        + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000;INIT=SET TIME ZONE"
                        + " 'UTC'");
        jdbc = new JdbcTemplate(source);
        transaction = new TransactionTemplate(new DataSourceTransactionManager(source));
        // Keep DDL session alive: H2 CHECK constant-set comparison otherwise retains a closed
        // session.
        schema = source.getConnection();
        ScriptUtils.executeSqlScript(
                schema, new ClassPathResource("trade-sql/V1__trade_order.sql"));
        ScriptUtils.executeSqlScript(
                schema, new ClassPathResource("trade-sql/V9__promotion_versions_usage.sql"));
        var c = new MybatisConfiguration();
        c.setMapUnderscoreToCamelCase(true);
        var bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(source);
        bean.setConfiguration(c);
        var factory = bean.getObject();
        c.addMapper(PromotionMapper.class);
        mapper = new SqlSessionTemplate(factory).getMapper(PromotionMapper.class);
        gateway = proxy(new PromotionGatewayImpl(mapper));
        admin = proxy(new PromotionAdministrationService(gateway));
        usage = proxy(new PromotionUsageService(gateway));
        now = tx(() -> gateway.nowEpochMs());
    }

    @AfterEach
    void cleanup() throws Exception {
        jdbc.execute("DROP ALL OBJECTS");
        schema.close();
    }

    private String request() {
        return "R" + (++requests);
    }

    private PromotionDraft draft(Long g, Long m) {
        return new PromotionDraft(
                "campaign",
                new PromotionScope(PromotionScope.Kind.SKU_IDS, List.of(2L, 1L)),
                new DiscountRule.FullReduction(List.of(new DiscountRule.Tier(100, 10))),
                PromotionVersion.CouponPolicy.ALLOW_ONE,
                10,
                now - 10000,
                now + 3600000,
                g,
                m);
    }

    private PromotionAdminReceipt create(Long g, Long m) {
        return admin.create(7, request(), "C" + requests, draft(g, m));
    }

    private PromotionAdminReceipt publish(PromotionAdminReceipt r) {
        return admin.publish(7, request(), r.id(), r.revision());
    }

    private PublishedPromotion version(PromotionAdminReceipt r) {
        return tx(() -> gateway.version(r.id(), r.activeVersion()));
    }

    private void order(String n, long m) {
        jdbc.update(
                "INSERT INTO trade_order(order_no,member_id,total_amount,status)"
                    + " VALUES(?,?,1.00,'RESERVING')",
                n,
                m);
    }

    private void status(String n, String s) {
        jdbc.update("UPDATE trade_order SET status=? WHERE order_no=?", s, n);
    }

    private PromotionUsage hold(String n, long m, PromotionAdminReceipt r) {
        var v = version(r);
        return tx(() -> usage.hold(n, m, r.id(), v.version(), 10, v.ruleHash()));
    }

    private PromotionCounter counter(long id) {
        return tx(() -> gateway.lockCounter(id));
    }

    private long count(String sql, Object... a) {
        return jdbc.queryForObject(sql, Long.class, a);
    }

    private void code(PromotionPersistenceException.Code c, Runnable action) {
        assertEquals(c, assertThrows(PromotionPersistenceException.class, action::run).code());
    }

    @Test
    void publishPersistsImmutableRulesScopeHashAndOriginalVersions() {
        var r = publish(create(10L, 2L));
        var v = version(r);
        assertEquals(List.of(1L, 2L), v.draft().scope().ids());
        assertEquals(2, count("SELECT COUNT(*) FROM trade_promotion_scope"));
        var saved = admin.saveDraft(7, request(), r.id(), r.revision(), draft(20L, 3L));
        assertEquals(v, version(r));
        var next = publish(saved);
        assertEquals(2, next.activeVersion());
        assertEquals(v, version(r));
        assertNotEquals(v.ruleHash(), version(next).ruleHash());
        assertEquals(2, count("SELECT COUNT(*) FROM trade_promotion_version"));
    }

    @Test
    void adminReplaysBeforeRevisionAndChangedContentsReject() {
        var r = create(10L, 2L);
        var p = admin.publish(7, "PUB", r.id(), 0);
        admin.changeState(7, request(), r.id(), 1, PromotionRoot.State.PAUSED);
        assertEquals(p, admin.publish(7, "PUB", r.id(), 0));
        code(ADMIN_REQUEST_CONFLICT, () -> admin.publish(7, "PUB", r.id(), 1));
        code(
                ADMIN_REQUEST_CONFLICT,
                () -> admin.changeState(7, "PUB", r.id(), 0, PromotionRoot.State.ARCHIVED));
        code(
                PROMOTION_REVISION_CONFLICT,
                () -> admin.saveDraft(7, "STALE", r.id(), 0, draft(10L, 2L)));
        assertEquals(
                0,
                count(
                        "SELECT COUNT(*) FROM trade_promotion_admin_request WHERE"
                            + " request_no='STALE'"));
    }

    @Test
    void createRetriesOnceAndOperatorNamespacesAreIndependent() {
        var d = draft(null, null);
        var a = admin.create(7, "CREATE", "A", d);
        assertEquals(a, admin.create(7, "CREATE", "A", d));
        code(ADMIN_REQUEST_CONFLICT, () -> admin.create(7, "CREATE", "B", d));
        assertNotEquals(a.id(), admin.create(8, "CREATE", "B", d).id());
        assertEquals(2, count("SELECT COUNT(*) FROM trade_promotion_root"));
        assertEquals(
                2,
                count(
                        "SELECT COUNT(*) FROM trade_promotion_admin_request WHERE"
                            + " created_at_epoch_ms>0 AND"
                            + " completed_at_epoch_ms>=created_at_epoch_ms"));
    }

    @Test
    void duplicateCodeFailureRollsBackRequestSentinel() {
        admin.create(7, "A", "SAME", draft(null, null));
        assertThrows(RuntimeException.class, () -> admin.create(7, "B", "SAME", draft(null, null)));
        assertEquals(
                0,
                count("SELECT COUNT(*) FROM trade_promotion_admin_request WHERE request_no='B'"));
        assertNotNull(admin.create(7, "B", "OTHER", draft(null, null)));
    }

    @Test
    void holdReplayKeepsOriginalAndChangedBindingRejects() {
        var r = publish(create(2L, 2L));
        order("O1", 1);
        var u = hold("O1", 1, r);
        assertEquals(u, hold("O1", 1, r));
        assertEquals(1, counter(r.id()).held());
        code(
                PROMOTION_USAGE_CONFLICT,
                () -> tx(() -> usage.hold("O1", 1, r.id(), 1, 11, u.ruleHash())));
        code(
                PROMOTION_USAGE_CONFLICT,
                () -> tx(() -> usage.hold("O1", 2, r.id(), 1, 10, u.ruleHash())));
        assertEquals(1, count("SELECT COUNT(*) FROM trade_promotion_usage_event"));
    }

    @Test
    void quotasCountHeldAndUsedAndPartialTakeRollsBack() {
        var r = publish(create(3L, 1L));
        order("O1", 1);
        order("O2", 1);
        order("O3", 2);
        hold("O1", 1, r);
        code(PROMOTION_QUOTA_CHANGED, () -> hold("O2", 1, r));
        assertEquals(1, counter(r.id()).held());
        status("O1", "PAID");
        tx(() -> usage.confirm("O1", r.id(), "PAID"));
        code(PROMOTION_QUOTA_CHANGED, () -> hold("O2", 1, r));
        hold("O3", 2, r);
        assertEquals(1, counter(r.id()).held());
        assertEquals(1, counter(r.id()).used());
        assertEquals(0, count("SELECT COUNT(*) FROM trade_promotion_usage WHERE order_no='O2'"));
    }

    @Test
    void concurrentLastGlobalSlotHasOneWinner() throws Exception {
        var r = publish(create(1L, 1L));
        order("O1", 1);
        order("O2", 2);
        var result = race(() -> hold("O1", 1, r), () -> hold("O2", 2, r));
        assertEquals(1, result.stream().filter(x -> x instanceof PromotionUsage).count());
        assertEquals(1, result.stream().filter(x -> x == PROMOTION_QUOTA_CHANGED).count());
        assertEquals(1, counter(r.id()).held());
        assertEquals(1, count("SELECT COUNT(*) FROM trade_promotion_usage"));
    }

    @Test
    void concurrentMemberLimitWithUnlimitedGlobalHasOneWinner() throws Exception {
        var r = publish(create(null, 1L));
        order("O1", 1);
        order("O2", 1);
        var result = race(() -> hold("O1", 1, r), () -> hold("O2", 1, r));
        assertEquals(1, result.stream().filter(x -> x instanceof PromotionUsage).count());
        assertEquals(1, result.stream().filter(x -> x == PROMOTION_QUOTA_CHANGED).count());
        assertEquals(1, counter(r.id()).held());
    }

    @Test
    void newVersionNeverResetsQuotaAndOldHoldStillFinishes() {
        var r = publish(create(1L, 1L));
        order("O1", 1);
        hold("O1", 1, r);
        var next = publish(admin.saveDraft(7, request(), r.id(), r.revision(), draft(1L, 1L)));
        order("O2", 2);
        code(PROMOTION_QUOTA_CHANGED, () -> hold("O2", 2, next));
        status("O1", "CLOSED");
        tx(() -> usage.release("O1", r.id(), "CLOSED"));
        hold("O2", 2, next);
        assertEquals(1, counter(r.id()).held());
        assertEquals(2, tx(() -> gateway.lockUsage("O2", r.id())).version());
    }

    @Test
    void loweredGlobalAndMemberLimitsCannotDiscardUsage() {
        var r = publish(create(null, null));
        order("O1", 1);
        order("O2", 1);
        hold("O1", 1, r);
        hold("O2", 1, r);
        var a = admin.saveDraft(7, request(), r.id(), r.revision(), draft(1L, 1L));
        code(PROMOTION_LIMIT_BELOW_USAGE, () -> publish(a));
        var b = admin.saveDraft(7, request(), r.id(), a.revision(), draft(null, 1L));
        code(PROMOTION_LIMIT_BELOW_USAGE, () -> publish(b));
        assertEquals(1, count("SELECT COUNT(*) FROM trade_promotion_version"));
        assertNull(counter(r.id()).globalLimit());
        assertNull(counter(r.id()).memberLimit());
    }

    @Test
    void pauseResumeArchivePreserveHeldResourcesAndVersion() {
        var r = publish(create(2L, 1L));
        order("O1", 1);
        hold("O1", 1, r);
        var paused =
                admin.changeState(7, request(), r.id(), r.revision(), PromotionRoot.State.PAUSED);
        order("O2", 2);
        code(PROMOTION_SNAPSHOT_STALE, () -> hold("O2", 2, r));
        var resumed =
                admin.changeState(
                        7, request(), r.id(), paused.revision(), PromotionRoot.State.PUBLISHED);
        assertEquals(r.activeVersion(), resumed.activeVersion());
        var archived =
                admin.changeState(
                        7, request(), r.id(), resumed.revision(), PromotionRoot.State.ARCHIVED);
        code(
                PROMOTION_STATE_CONFLICT,
                () ->
                        admin.changeState(
                                7,
                                request(),
                                r.id(),
                                archived.revision(),
                                PromotionRoot.State.PUBLISHED));
        status("O1", "PAID");
        tx(() -> usage.confirm("O1", r.id(), "PAID"));
        assertEquals(1, counter(r.id()).used());
    }

    @Test
    void wrongHashAndReplacedVersionRejectNewHold() {
        var r = publish(create(null, null));
        order("O1", 1);
        var v = version(r);
        code(
                PROMOTION_SNAPSHOT_STALE,
                () -> tx(() -> usage.hold("O1", 1, r.id(), 1, 10, "0".repeat(64))));
        publish(admin.saveDraft(7, request(), r.id(), r.revision(), draft(null, null)));
        code(
                PROMOTION_SNAPSHOT_STALE,
                () -> tx(() -> usage.hold("O1", 1, r.id(), 1, 10, v.ruleHash())));
        assertEquals(0, counter(r.id()).held());
        assertEquals(0, count("SELECT COUNT(*) FROM trade_promotion_member_counter"));
    }

    @Test
    void terminalRequiresDurableOrderStateAndNeverReturnsUsedQuota() {
        var r = publish(create(1L, 1L));
        order("O1", 1);
        hold("O1", 1, r);
        code(PROMOTION_USAGE_CONFLICT, () -> tx(() -> usage.confirm("O1", r.id(), "UNVERIFIED")));
        code(PROMOTION_USAGE_CONFLICT, () -> tx(() -> usage.release("O1", r.id(), "TTL")));
        status("O1", "PAID");
        var u = tx(() -> usage.confirm("O1", r.id(), "PAID"));
        assertEquals(u, tx(() -> usage.confirm("O1", r.id(), "RETRY")));
        code(
                PROMOTION_USAGE_CONFLICT,
                () -> tx(() -> usage.release("O1", r.id(), "REFUND_REQUESTED")));
        status("O1", "CLOSED");
        code(
                PROMOTION_USAGE_CONFLICT,
                () -> tx(() -> usage.release("O1", r.id(), "INCONSISTENT_CLOSE")));
        assertEquals(1, counter(r.id()).used());
        assertEquals(2, count("SELECT COUNT(*) FROM trade_promotion_usage_event"));
    }

    @Test
    void releasedUsageIsIdempotentAndCannotBeResurrected() {
        var r = publish(create(1L, 1L));
        order("O1", 1);
        hold("O1", 1, r);
        status("O1", "CLOSED");
        var u = tx(() -> usage.release("O1", r.id(), "CLOSED"));
        assertEquals(u, tx(() -> usage.release("O1", r.id(), "RETRY")));
        assertEquals(u, hold("O1", 1, r));
        status("O1", "PAID");
        code(PROMOTION_USAGE_CONFLICT, () -> tx(() -> usage.confirm("O1", r.id(), "LATE_PAY")));
        assertEquals(0, counter(r.id()).held());
        assertEquals(0, counter(r.id()).used());
    }

    @Test
    void laterOutboxFailureRollsBackOrderCounterUsageAndAudit() {
        var r = publish(create(1L, 1L));
        var v = version(r);
        assertThrows(
                IllegalStateException.class,
                () ->
                        tx(
                                () -> {
                                    order("O1", 1);
                                    usage.hold("O1", 1, r.id(), 1, 10, v.ruleHash());
                                    throw new IllegalStateException(
                                            "simulated later outbox write failure");
                                }));
        assertEquals(0, count("SELECT COUNT(*) FROM trade_order"));
        assertEquals(0, counter(r.id()).held());
        assertEquals(0, count("SELECT COUNT(*) FROM trade_promotion_usage"));
        assertEquals(0, count("SELECT COUNT(*) FROM trade_promotion_usage_event"));
        assertEquals(0, count("SELECT COUNT(*) FROM trade_promotion_member_counter"));
    }

    @Test
    void auditFailureRollsBackCounterUsageAndOrderStatus() {
        var r = publish(create(1L, 1L));
        order("O1", 1);
        hold("O1", 1, r);
        jdbc.update(
                "INSERT INTO"
                    + " trade_promotion_usage_event(order_no,root_id,from_state,target_state,reason,occurred_at_epoch_ms)"
                    + " VALUES('O1',?,'HELD','USED','constraint fault',?)",
                r.id(),
                now);
        assertThrows(
                RuntimeException.class,
                () ->
                        tx(
                                () -> {
                                    status("O1", "PAID");
                                    return usage.confirm("O1", r.id(), "PAID");
                                }));
        assertEquals(
                "RESERVING",
                jdbc.queryForObject(
                        "SELECT status FROM trade_order WHERE order_no='O1'", String.class));
        assertEquals(PromotionUsage.State.HELD, tx(() -> gateway.lockUsage("O1", r.id())).state());
        assertEquals(1, counter(r.id()).held());
        assertEquals(0, counter(r.id()).used());
    }

    @Test
    void standaloneUsageCallRequiresOuterTransaction() {
        var r = publish(create(1L, 1L));
        order("O1", 1);
        var v = version(r);
        assertThrows(
                org.springframework.transaction.IllegalTransactionStateException.class,
                () -> usage.hold("O1", 1, r.id(), 1, 10, v.ruleHash()));
        assertThrows(
                org.springframework.transaction.IllegalTransactionStateException.class,
                () -> usage.release("O1", r.id(), "CLOSED"));
        assertEquals(0, counter(r.id()).held());
    }

    @Test
    void concurrentAdminReplayAndRevisionRaceHaveOneEffect() throws Exception {
        var r = create(10L, 1L);
        var a =
                race(
                        () -> admin.publish(7, "SAME", r.id(), 0),
                        () -> admin.publish(7, "SAME", r.id(), 0));
        assertEquals(a.get(0), a.get(1));
        assertEquals(1, count("SELECT COUNT(*) FROM trade_promotion_version"));
        var b =
                race(
                        () -> admin.saveDraft(7, "A", r.id(), 1, draft(20L, 2L)),
                        () -> admin.saveDraft(7, "B", r.id(), 1, draft(30L, 3L)));
        assertEquals(1, b.stream().filter(x -> x instanceof PromotionAdminReceipt).count());
        assertEquals(1, b.stream().filter(x -> x == PROMOTION_REVISION_CONFLICT).count());
    }

    @Test
    void storedHashAndUnknownFieldsReject() {
        var r = publish(create(null, null));
        jdbc.update(
                "UPDATE trade_promotion_version SET rule_hash=? WHERE root_id=?",
                "0".repeat(64),
                r.id());
        assertThrows(IllegalStateException.class, () -> version(r));
        var text = PromotionConverter.encode(draft(null, null));
        assertThrows(
                RuntimeException.class,
                () ->
                        PromotionConverter.decode(
                                text.substring(0, text.length() - 1) + ",\"unexpected\":true}"));
    }

    @Test
    void databaseCheckAndForeignKeysRejectInvalidWrites() {
        var r = publish(create(1L, 1L));
        assertThrows(
                RuntimeException.class,
                () ->
                        jdbc.update(
                                "UPDATE trade_promotion_counter SET held_count=-1 WHERE root_id=?",
                                r.id()));
        assertThrows(
                RuntimeException.class,
                () ->
                        jdbc.update(
                                "UPDATE trade_promotion_counter SET held_count=2 WHERE root_id=?",
                                r.id()));
        assertThrows(
                RuntimeException.class,
                () ->
                        jdbc.update(
                                "INSERT INTO"
                                    + " trade_promotion_usage(order_no,root_id,version,member_id,discount_fen,rule_hash,state,created_at_epoch_ms,updated_at_epoch_ms)"
                                    + " VALUES('MISSING',?,1,1,10,?,'HELD',?,?)",
                                r.id(),
                                version(r).ruleHash(),
                                now,
                                now));
    }

    @Test
    void futureExpiredAndLateCallbackWindowsRespectDurableHolds() {
        var d = draft(null, null);
        var future =
                new PromotionDraft(
                        d.name(),
                        d.scope(),
                        d.rule(),
                        d.couponPolicy(),
                        10,
                        now + 3600000,
                        now + 7200000,
                        null,
                        null);
        var upcoming = publish(admin.create(7, request(), "UPCOMING", future));
        order("O1", 1);
        code(PROMOTION_SNAPSHOT_STALE, () -> hold("O1", 1, upcoming));
        var past =
                new PromotionDraft(
                        d.name(),
                        d.scope(),
                        d.rule(),
                        d.couponPolicy(),
                        10,
                        now - 20000,
                        now - 10000,
                        null,
                        null);
        var expired = admin.create(7, request(), "EXPIRED", past);
        code(PROMOTION_STATE_CONFLICT, () -> publish(expired));
        assertEquals(
                0,
                count(
                        "SELECT COUNT(*) FROM trade_promotion_version WHERE root_id=?",
                        expired.id()));
        PromotionGateway advanced =
                proxy(
                        new PromotionGatewayImpl(mapper) {
                            @Override
                            public long nowEpochMs() {
                                return now + 7200000;
                            }
                        });
        var lateAdmin = proxy(new PromotionAdministrationService(advanced));
        var lateUsage = proxy(new PromotionUsageService(advanced));
        var live = publish(create(null, null));
        order("O2", 2);
        hold("O2", 2, live);
        var paused =
                admin.changeState(
                        7, request(), live.id(), live.revision(), PromotionRoot.State.PAUSED);
        code(
                PROMOTION_STATE_CONFLICT,
                () ->
                        lateAdmin.changeState(
                                7,
                                request(),
                                live.id(),
                                paused.revision(),
                                PromotionRoot.State.PUBLISHED));
        status("O2", "PAID");
        tx(() -> lateUsage.confirm("O2", live.id(), "LATE_CALLBACK"));
        assertEquals(1, counter(live.id()).used());
        code(
                PROMOTION_SNAPSHOT_STALE,
                () ->
                        tx(
                                () ->
                                        lateUsage.hold(
                                                "O1",
                                                1,
                                                upcoming.id(),
                                                1,
                                                10,
                                                version(upcoming).ruleHash())));
    }

    @Test
    void concurrentDuplicateTerminalsWriteOneEvent() throws Exception {
        var r = publish(create(2L, 2L));
        order("O1", 1);
        hold("O1", 1, r);
        status("O1", "PAID");
        var a =
                race(
                        () -> tx(() -> usage.confirm("O1", r.id(), "PAID")),
                        () -> tx(() -> usage.confirm("O1", r.id(), "RETRY")));
        assertEquals(a.get(0), a.get(1));
        order("O2", 2);
        hold("O2", 2, r);
        status("O2", "CLOSED");
        var b =
                race(
                        () -> tx(() -> usage.release("O2", r.id(), "CLOSED")),
                        () -> tx(() -> usage.release("O2", r.id(), "RETRY")));
        assertEquals(b.get(0), b.get(1));
        assertEquals(0, counter(r.id()).held());
        assertEquals(1, counter(r.id()).used());
        assertEquals(4, count("SELECT COUNT(*) FROM trade_promotion_usage_event"));
    }

    @Test
    void competingPaidAndClosedTransactionsCannotBothFinish() throws Exception {
        var r = publish(create(1L, 1L));
        order("O1", 1);
        hold("O1", 1, r);
        var a =
                race(
                        () ->
                                tx(
                                        () -> {
                                            if (!"RESERVING"
                                                    .equals(gateway.lockOrder("O1").status()))
                                                return "SKIPPED";
                                            status("O1", "PAID");
                                            return usage.confirm("O1", r.id(), "PAID");
                                        }),
                        () ->
                                tx(
                                        () -> {
                                            if (!"RESERVING"
                                                    .equals(gateway.lockOrder("O1").status()))
                                                return "SKIPPED";
                                            status("O1", "CLOSED");
                                            return usage.release("O1", r.id(), "VERIFIED_CLOSE");
                                        }));
        assertEquals(1, a.stream().filter(x -> x instanceof PromotionUsage).count());
        assertEquals(1, a.stream().filter("SKIPPED"::equals).count());
        var u = tx(() -> gateway.lockUsage("O1", r.id()));
        assertEquals(0, counter(r.id()).held());
        assertEquals(u.state() == PromotionUsage.State.USED ? 1 : 0, counter(r.id()).used());
        assertEquals(2, count("SELECT COUNT(*) FROM trade_promotion_usage_event"));
    }

    @Test
    void publishingLowerLimitAndHoldingUseTheSameRootLock() throws Exception {
        var r = publish(create(2L, 2L));
        order("O1", 1);
        order("O2", 2);
        hold("O1", 1, r);
        var saved = admin.saveDraft(7, request(), r.id(), r.revision(), draft(1L, 1L));
        var a = race(() -> publish(saved), () -> hold("O2", 2, r));
        assertEquals(
                1,
                a.stream()
                        .filter(
                                x ->
                                        x instanceof PromotionUsage
                                                || x instanceof PromotionAdminReceipt)
                        .count());
        assertEquals(
                1,
                a.stream()
                        .filter(
                                x ->
                                        x == PROMOTION_LIMIT_BELOW_USAGE
                                                || x == PROMOTION_SNAPSHOT_STALE)
                        .count());
        var c = counter(r.id());
        assertTrue(c.held() + c.used() <= c.globalLimit());
    }

    private List<Object> race(Callable<?> a, Callable<?> b) throws Exception {
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var futures = new ArrayList<Future<Object>>();
            for (var action : List.of(a, b))
                futures.add(
                        pool.submit(
                                () -> {
                                    if (!start.await(5, TimeUnit.SECONDS))
                                        throw new IllegalStateException("barrier timeout");
                                    try {
                                        return action.call();
                                    } catch (PromotionPersistenceException e) {
                                        return e.code();
                                    }
                                }));
            start.countDown();
            return List.of(
                    futures.get(0).get(20, TimeUnit.SECONDS),
                    futures.get(1).get(20, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        }
    }
}
