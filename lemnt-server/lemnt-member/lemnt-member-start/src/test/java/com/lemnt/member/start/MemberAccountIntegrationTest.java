package com.lemnt.member.start;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.member.infrastructure.mapper.MemberMapper;
import com.lemnt.member.infrastructure.gateway.MemberGatewayImpl;
import com.lemnt.member.infrastructure.account.mapper.MemberAccountMapper;
import com.lemnt.member.infrastructure.account.gateway.*;
import com.lemnt.member.domain.account.gateway.*;
import com.lemnt.member.domain.account.model.*;
import com.lemnt.member.domain.gateway.MemberGateway;
import com.lemnt.member.domain.model.Member;
import com.lemnt.member.application.account.service.*;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class MemberAccountIntegrationTest {
    JdbcDataSource source;
    Connection schema;
    MemberAccountGateway gateway;
    MemberGateway members;
    MemberAccountService service;
    MemberAuthService auth;
    Long id, other;
    MemberVerificationGateway verification;
    MemberSmsChallengeService sms;
    MemberIdentityService identity;
    com.lemnt.member.application.account.executor.MemberAdminQueryExe adminQueries;
    final MemberSmsDeliveryGateway delivery =
            new MemberSmsDeliveryGateway() {
                public boolean configured() {
                    return true;
                }

                public String hash(String mobile, String scene, String code) {
                    return mobile + ":" + scene + ":" + code;
                }

                public void send(String mobile, String code) {}

                public int dailyLimit() {
                    return 100;
                }
            };

    @SuppressWarnings("unchecked")
    <T> T tx(T target) {
        var p = new ProxyFactory(target);
        p.addAdvice(
                new TransactionInterceptor(
                        new DataSourceTransactionManager(source),
                        new AnnotationTransactionAttributeSource()));
        return (T) p.getProxy();
    }

    @BeforeEach
    void setup() throws Exception {
        source = new JdbcDataSource();
        source.setURL(
                "jdbc:h2:mem:member_"
                        + UUID.randomUUID()
                        + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        schema = source.getConnection();
        ScriptUtils.executeSqlScript(schema, new ClassPathResource("member-sql/V1__member.sql"));
        ScriptUtils.executeSqlScript(
                schema, new ClassPathResource("member-sql/V2__member_account.sql"));
        ScriptUtils.executeSqlScript(
                schema, new ClassPathResource("member-sql/V3__member_verification.sql"));
        var config = new MybatisConfiguration();
        config.setMapUnderscoreToCamelCase(true);
        var bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(source);
        bean.setConfiguration(config);
        bean.setGlobalConfig(new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
        var factory = bean.getObject();
        config.addMapper(MemberMapper.class);
        config.addMapper(MemberAccountMapper.class);
        config.addMapper(
                com.lemnt.member.infrastructure.account.mapper.MemberVerificationMapper.class);
        config.addMapper(
                com.lemnt.member.infrastructure.account.mapper.MemberAdminQueryMapper.class);
        var template = new SqlSessionTemplate(factory);
        members = new MemberGatewayImpl(template.getMapper(MemberMapper.class));
        gateway = new MemberAccountGatewayImpl(template.getMapper(MemberAccountMapper.class));
        service = tx(new MemberAccountService(gateway));
        auth = tx(new MemberAuthService(gateway, new MemberPasswordGatewayImpl()));
        verification =
                new MemberVerificationGatewayImpl(
                        template.getMapper(
                                com.lemnt.member.infrastructure.account.mapper
                                        .MemberVerificationMapper.class));
        sms = tx(new MemberSmsChallengeService(verification, gateway, delivery));
        identity = tx(new MemberIdentityService(verification, gateway, members, auth, sms));
        adminQueries =
                new com.lemnt.member.application.account.executor.MemberAdminQueryExe(
                        new MemberAdminQueryGatewayImpl(
                                template.getMapper(
                                        com.lemnt.member.infrastructure.account.mapper
                                                .MemberAdminQueryMapper.class)));
        id = members.save(Member.register("M1", "one", "13800000001", null, null)).id();
        other = members.save(Member.register("M2", "two", "13800000002", null, null)).id();
    }

    @AfterEach
    void close() throws Exception {
        schema.close();
    }

    void sql(String text) throws Exception {
        try (var s = schema.createStatement()) {
            s.execute(text);
        }
    }

    int count(String table) throws Exception {
        try (var s = schema.createStatement();
                var q = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            q.next();
            return q.getInt(1);
        }
    }

    Address address(Long member, Long key, boolean def) {
        return new Address(key, member, "收件人", "13800000003", 110101L, "测试地址", def);
    }

    TokenPair login() {
        auth.resetPassword(id, "Member-password-2026");
        return auth.login("13800000001", "Member-password-2026");
    }

    @Test
    void passwordLoginIssuesSeparateOpaqueHashedSessionsAndRejectsAdminTokens() throws Exception {
        var tokens = login();
        assertTrue(tokens.accessToken().startsWith("mbr_a_"));
        assertTrue(tokens.refreshToken().startsWith("mbr_r_"));
        assertEquals(id, auth.authenticate(tokens.accessToken()));
        assertNull(auth.authenticate(tokens.refreshToken()));
        assertNull(auth.authenticate("eyJ.admin.jwt"));
        var session =
                gateway.sessionRefresh(
                        java.util.HexFormat.of()
                                .formatHex(
                                        java.security.MessageDigest.getInstance("SHA-256")
                                                .digest(
                                                        tokens.refreshToken()
                                                                .getBytes(
                                                                        java.nio.charset
                                                                                .StandardCharsets
                                                                                .UTF_8))));
        assertNotNull(session);
        assertFalse(session.accessHash().contains(tokens.accessToken()));
        assertEquals(64, session.accessHash().length());
        assertEquals(1, count("member_session"));
    }

    @Test
    void refreshRotatesBothTokensAndCannotBeReplayed() {
        var first = login();
        var next = auth.refresh(first.refreshToken());
        assertNull(auth.authenticate(first.accessToken()));
        assertEquals(id, auth.authenticate(next.accessToken()));
        assertThrows(SecurityException.class, () -> auth.refresh(first.refreshToken()));
    }

    @Test
    void concurrentRefreshHasExactlyOneWinner() throws Exception {
        var tokens = login();
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> task =
                    () -> {
                        start.await();
                        try {
                            auth.refresh(tokens.refreshToken());
                            return true;
                        } catch (SecurityException ex) {
                            return false;
                        }
                    };
            var a = pool.submit(task);
            var b = pool.submit(task);
            start.countDown();
            assertNotEquals(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
            assertEquals(1, count("member_session"));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void logoutAndPasswordChangeRevokeSessions() {
        var first = login();
        var second = auth.login("13800000001", "Member-password-2026");
        assertTrue(auth.logout("Bearer " + first.accessToken()));
        assertTrue(auth.logout("Bearer " + first.accessToken()));
        assertNull(auth.authenticate(first.accessToken()));
        assertEquals(id, auth.authenticate(second.accessToken()));
        auth.changePassword(id, "Member-password-2026", "New-password-2026");
        assertNull(auth.authenticate(second.accessToken()));
        assertThrows(
                SecurityException.class, () -> auth.login("13800000001", "Member-password-2026"));
        assertNotNull(auth.login("13800000001", "New-password-2026"));
    }

    @Test
    void disabledMemberCannotRetainOrRegainOldSession() {
        var tokens = login();
        service.profileUpdate(id, "one", null, null, null, null, 0);
        assertNull(auth.authenticate(tokens.accessToken()));
        assertThrows(SecurityException.class, () -> auth.refresh(tokens.refreshToken()));
        service.profileUpdate(id, "one", null, null, null, null, 1);
        assertNull(auth.authenticate(tokens.accessToken()));
    }

    @Test
    void repeatedFailedLoginCommitsRateLimitEvenThoughLoginThrows() throws Exception {
        auth.resetPassword(id, "Member-password-2026");
        for (int n = 0; n < 5; n++)
            assertThrows(
                    SecurityException.class, () -> auth.login("13800000001", "wrong-password"));
        assertThrows(
                SecurityException.class, () -> auth.login("13800000001", "Member-password-2026"));
        assertEquals(0, count("member_session"));
        sql("UPDATE member_login_attempt SET window_start=TIMESTAMP '2000-01-01 00:00:00'");
        assertNotNull(auth.login("13800000001", "Member-password-2026"));
    }

    @Test
    void addressOwnershipAndDefaultAreEnforced() {
        var first = service.saveAddress(address(id, null, true));
        var second = service.saveAddress(address(id, null, true));
        assertFalse(gateway.address(id, first.id()).defaultStatus());
        assertTrue(gateway.address(id, second.id()).defaultStatus());
        assertNull(service.address(other, first.id()));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.saveAddress(address(other, first.id(), true)));
        assertFalse(service.deleteAddress(other, first.id()));
        assertEquals(2, service.addresses(id).size());
    }

    @Test
    void concurrentDefaultsNeverLeaveTwoDefaultAddresses() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> service.saveAddress(address(id, null, true)));
            var b = pool.submit(() -> service.saveAddress(address(id, null, true)));
            a.get(15, TimeUnit.SECONDS);
            b.get(15, TimeUnit.SECONDS);
            assertEquals(1, gateway.addresses(id).stream().filter(Address::defaultStatus).count());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void addressLimitIsCheckedBeforeClearingExistingDefault() {
        for (int n = 0; n < 20; n++) service.saveAddress(address(id, null, n == 0));
        assertThrows(
                IllegalStateException.class, () -> service.saveAddress(address(id, null, true)));
        assertEquals(20, gateway.addresses(id).size());
        assertEquals(1, gateway.addresses(id).stream().filter(Address::defaultStatus).count());
    }

    @Test
    void pointRequestIsIdempotentAndChangedContentCannotReplay() {
        var first = service.adjustAsset(id, "POINT", 10, "R1", "奖励");
        service.adjustAsset(id, "POINT", 2, "R2", "奖励2");
        assertEquals(first, service.adjustAsset(id, "POINT", 10, "R1", "奖励"));
        assertEquals(12, gateway.profile(id).points());
        assertThrows(
                IllegalStateException.class,
                () -> service.adjustAsset(id, "POINT", 11, "R1", "奖励"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.adjustAsset(id, "POINT", -13, "R3", "扣减"));
        assertEquals(12, gateway.profile(id).points());
    }

    @Test
    void concurrentPointRequestCreditsExactlyOnce() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> service.adjustAsset(id, "POINT", 10, "R1", "奖励"));
            var b = pool.submit(() -> service.adjustAsset(id, "POINT", 10, "R1", "奖励"));
            assertEquals(a.get(15, TimeUnit.SECONDS).id(), b.get(15, TimeUnit.SECONDS).id());
            assertEquals(10, gateway.profile(id).points());
            assertEquals(1, count("member_asset_record"));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void pointsOverflowAndLedgerFailureRollbackBalance() throws Exception {
        sql("UPDATE member_user SET points=9223372036854775807 WHERE id=" + id);
        assertThrows(
                ArithmeticException.class,
                () -> service.adjustAsset(id, "POINT", 1, "R1", "overflow"));
        assertEquals(0, count("member_asset_record"));
        sql("UPDATE member_user SET points=0 WHERE id=" + id);
        sql("ALTER TABLE member_asset_record ADD CONSTRAINT injected_failure CHECK(delta<0)");
        assertThrows(Exception.class, () -> service.adjustAsset(id, "POINT", 1, "R2", "rollback"));
        assertEquals(0, gateway.profile(id).points());
    }

    @Test
    void levelThresholdsExperienceAndManualAssignmentHaveAuditAndReplay() {
        Long level = service.saveLevel(new Level(null, "Gold", 1, 100L, 90, null, null, 1));
        service.adjustAsset(id, "EXPERIENCE", 110, "E1", "升级");
        assertEquals(level, gateway.profile(id).levelId());
        assertEquals(1, gateway.levelRecordCount(id));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.saveLevel(new Level(null, "Silver", 2, 50L, 95, null, null, 1)));
        assertFalse(service.deleteLevel(level));
        service.assignLevel(id, null, "L1", "取消等级");
        assertNull(gateway.profile(id).levelId());
        assertEquals(0, gateway.profile(id).experience());
        service.adjustAsset(id, "EXPERIENCE", 2, "E2", "after");
        service.assignLevel(id, null, "L1", "取消等级");
        assertEquals(2, gateway.profile(id).experience());
        assertThrows(
                IllegalStateException.class, () -> service.assignLevel(id, level, "L1", "取消等级"));
    }

    @Test
    void groupAndTagsEnforceReferencesAndOwnership() {
        Long group = service.saveGroup(new Group(null, "VIP", null, 1));
        Long tag = service.saveTag(new Tag(null, "active", null));
        service.assignGroup(id, group);
        service.assignTags(id, List.of(tag));
        assertFalse(service.deleteGroup(group));
        assertFalse(service.deleteTag(tag));
        assertEquals(List.of(tag), gateway.memberTags(id));
        service.assignTags(id, List.of());
        service.assignGroup(id, null);
        assertTrue(service.deleteTag(tag));
        assertTrue(service.deleteGroup(group));
    }

    @Test
    void dailySignInIsIdempotentAndCreditsBothAssetsAtomically() throws Exception {
        service.saveSignConfig(new SignConfig(1, 10L, 2L, 1));
        var first = service.sign(id);
        assertEquals(first, service.sign(id));
        assertEquals(10, gateway.profile(id).points());
        assertEquals(2, gateway.profile(id).experience());
        assertEquals(1, count("member_sign_record"));
        assertEquals(2, count("member_asset_record"));
    }

    @Test
    void concurrentSignInAwardsOnce() throws Exception {
        service.saveSignConfig(new SignConfig(1, 10L, 2L, 1));
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> service.sign(id));
            var b = pool.submit(() -> service.sign(id));
            assertEquals(a.get(15, TimeUnit.SECONDS).id(), b.get(15, TimeUnit.SECONDS).id());
            assertEquals(10, gateway.profile(id).points());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void failedExperienceLedgerRollsBackSignAndPoints() throws Exception {
        service.saveSignConfig(new SignConfig(1, 10L, 2L, 1));
        sql(
                "ALTER TABLE member_asset_record ADD CONSTRAINT reject_experience"
                        + " CHECK(asset_type!='EXPERIENCE')");
        assertThrows(Exception.class, () -> service.sign(id));
        assertEquals(0, count("member_sign_record"));
        assertEquals(0, gateway.profile(id).points());
        assertEquals(0, count("member_asset_record"));
    }

    @Test
    void profileValidationDoesNotAllowFutureBirthdayOrUnsafeAvatar() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.profileUpdate(id, "x", null, "javascript:bad", null, null, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.profileUpdate(id, "x", null, null, 9, null, null));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.profileUpdate(
                                id,
                                "x",
                                null,
                                null,
                                0,
                                gateway.now().toLocalDate().plusDays(1),
                                null));
        assertTrue(
                service.profileUpdate(
                        id,
                        "new",
                        "valid@example.com",
                        "https://example.test/avatar",
                        1,
                        null,
                        null));
        assertEquals("valid@example.com", gateway.profile(id).email());
    }

    @Test
    void smsCodeIsScopedSingleUseAndCooldownIsDurable() {
        var code = sms.prepare("13800000001", "LOGIN");
        assertThrows(IllegalStateException.class, () -> sms.prepare("13800000001", "LOGIN"));
        assertThrows(
                SecurityException.class,
                () -> sms.verify("13800000001", "RESET_PASSWORD", code, true));
        assertTrue(sms.verify("13800000001", "LOGIN", code, false));
        var tokens = identity.smsLogin("13800000001", code);
        assertEquals(id, auth.authenticate(tokens.accessToken()));
        assertThrows(SecurityException.class, () -> identity.smsLogin("13800000001", code));
    }

    @Test
    void smsFailedAttemptsCommitAndExpiredCodeFails() throws Exception {
        var code = sms.prepare("13800000001", "LOGIN");
        String wrong = code.equals("000000") ? "111111" : "000000";
        for (int i = 0; i < 5; i++)
            assertThrows(SecurityException.class, () -> identity.smsLogin("13800000001", wrong));
        assertThrows(SecurityException.class, () -> identity.smsLogin("13800000001", code));
        var second = sms.prepare("13800000002", "LOGIN");
        sql(
                "UPDATE member_sms_challenge SET expires_at=DATEADD('MINUTE',-1,CURRENT_TIMESTAMP)"
                    + " WHERE mobile='13800000002'");
        assertThrows(
                SecurityException.class, () -> sms.verify("13800000002", "LOGIN", second, true));
        assertThrows(IllegalArgumentException.class, () -> sms.prepare("13800000001", null));
    }

    @Test
    void smsGlobalBudgetIsEnforcedAcrossPhones() throws Exception {
        sql("INSERT INTO member_sms_budget(budget_day,requests) VALUES(CURRENT_DATE,99)");
        sms.prepare("13800000001", "LOGIN");
        assertThrows(IllegalStateException.class, () -> sms.prepare("13800000002", "LOGIN"));
        assertEquals(1, count("member_sms_challenge"));
    }

    @Test
    void smsPasswordResetAndMobileUpdateRevokeExistingSessions() {
        var first = login();
        var reset = sms.prepare("13800000001", "RESET_PASSWORD");
        assertTrue(identity.resetPassword("13800000001", reset, "Reset-password-2026"));
        assertNull(auth.authenticate(first.accessToken()));
        var second = auth.login("13800000001", "Reset-password-2026");
        var old = sms.prepare("13800000001", "UPDATE_OLD_MOBILE");
        var next = sms.prepare("13800000003", "UPDATE_NEW_MOBILE");
        assertThrows(
                SecurityException.class, () -> identity.mobile(id, "13800000003", next, "invalid"));
        assertEquals("13800000001", gateway.profile(id).mobile());
        assertTrue(identity.mobile(id, "13800000003", next, old));
        assertNull(auth.authenticate(second.accessToken()));
        assertEquals(
                id,
                auth.authenticate(auth.login("13800000003", "Reset-password-2026").accessToken()));
    }

    @Test
    void socialStatesAreNamespacedPurposeBoundAndCannotReplay() {
        String ns = "WECHAT_MINI:app-test";
        var proof = new SocialProof(ns, "openid-test", null);
        var state = identity.state(ns, "LOGIN", null);
        assertThrows(
                SecurityException.class,
                () ->
                        identity.socialLogin(
                                "WECHAT_MINI:other",
                                new SocialProof("WECHAT_MINI:other", "openid-test", null),
                                state));
        var token = identity.socialLogin(ns, proof, state);
        assertNotEquals(id, token.memberId());
        assertEquals(token.memberId(), auth.authenticate(token.accessToken()));
        assertThrows(SecurityException.class, () -> identity.socialLogin(ns, proof, state));
        assertThrows(IllegalStateException.class, () -> identity.unbind(token.memberId(), ns));
        var bind = identity.state(ns, "BIND", id);
        assertThrows(
                SecurityException.class,
                () -> identity.bind(other, ns, new SocialProof(ns, "another", null), bind));
        assertTrue(identity.bind(id, ns, new SocialProof(ns, "another", null), bind));
        var conflict = identity.state(ns, "BIND", other);
        assertThrows(
                IllegalStateException.class,
                () -> identity.bind(other, ns, new SocialProof(ns, "another", null), conflict));
    }

    @Test
    void expiredSocialStateAndWrongPhoneProofCannotIssueSessions() throws Exception {
        String ns = "WECHAT_MP:app-test";
        var state = identity.state(ns, "LOGIN", null);
        sql("UPDATE member_social_state SET expires_at=DATEADD('MINUTE',-1,CURRENT_TIMESTAMP)");
        assertThrows(
                SecurityException.class,
                () -> identity.socialLogin(ns, new SocialProof(ns, "openid", null), state));
        assertEquals(0, count("member_session"));
        var bind = identity.state(ns, "BIND", id);
        identity.bind(id, ns, new SocialProof(ns, "openid", null), bind);
        var login = identity.state(ns, "LOGIN", null);
        assertThrows(
                SecurityException.class,
                () ->
                        identity.socialLogin(
                                ns, new SocialProof(ns, "openid", "13800000002"), login));
        assertEquals(0, count("member_session"));
    }

    @Test
    void adminRecordPagesAreBoundedFilteredAndTyped() {
        service.adjustAsset(id, "POINT", 5L, "test-record", "test");
        service.adjustAsset(other, "POINT", 7L, "test-record", "test");
        service.adjustAsset(id, "EXPERIENCE", 3L, "test-record", "test");
        var all =
                adminQueries.assets(
                        new com.lemnt.member.application.account.qry.AdminMemberPageQry(
                                "POINT", null, null, null, 1, 1));
        assertEquals(2, all.total());
        assertEquals(1, all.records().size());
        var mine =
                adminQueries.assets(
                        new com.lemnt.member.application.account.qry.AdminMemberPageQry(
                                "POINT", id, null, null, 1, 20));
        assertEquals(1, mine.total());
        assertEquals(id, mine.records().get(0).memberId());
        assertNull(
                adminQueries.assetsDetail(
                        new com.lemnt.member.application.account.qry.AdminMemberRecordDetailQry(
                                "EXPERIENCE", mine.records().get(0).id())));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        adminQueries.assets(
                                new com.lemnt.member.application.account.qry.AdminMemberPageQry(
                                        "POINT", null, null, null, 1, 101)));
        assertEquals(
                0,
                adminQueries
                        .groups(
                                new com.lemnt.member.application.account.qry.AdminMemberPageQry(
                                        "GROUP", null, "absent", null, 1, 20))
                        .total());
    }

    @Test
    void allAdminMetadataAndRecordQueriesUseActualProductionSql() {
        var group = service.saveGroup(new Group(null, "VIP", "remark", 1));
        service.saveGroup(new Group(null, "inactive", null, 0));
        service.saveTag(new Tag(null, "premium", null));
        var page =
                new com.lemnt.member.application.account.qry.AdminMemberPageQry(
                        "GROUP", null, "VIP", 1, 1, 20);
        assertEquals(1, adminQueries.groups(page).total());
        assertEquals(group, adminQueries.groups(page).records().get(0).id());
        assertEquals(
                1,
                adminQueries
                        .tags(
                                new com.lemnt.member.application.account.qry.AdminMemberPageQry(
                                        "TAG", null, "premium", null, 1, 20))
                        .total());
        var level = service.saveLevel(new Level(null, "bronze", 1, 10L, 95, null, null, 1));
        service.assignLevel(id, level, "level-record-test", "test");
        var levels =
                adminQueries.levels(
                        new com.lemnt.member.application.account.qry.AdminMemberPageQry(
                                "LEVEL", id, null, null, 1, 20));
        assertEquals(1, levels.total());
        assertEquals(
                id,
                adminQueries
                        .levelsDetail(
                                new com.lemnt.member.application.account.qry
                                        .AdminMemberRecordDetailQry(
                                        "LEVEL", levels.records().get(0).id()))
                        .memberId());
        service.saveSignConfig(new SignConfig(1, 1L, 1L, 1));
        service.sign(id);
        assertEquals(
                1,
                adminQueries
                        .signs(
                                new com.lemnt.member.application.account.qry.AdminMemberPageQry(
                                        "SIGN", null, null, null, 1, 20))
                        .total());
        assertEquals(
                0,
                adminQueries
                        .signs(
                                new com.lemnt.member.application.account.qry.AdminMemberPageQry(
                                        "SIGN", other, null, null, 1, 20))
                        .total());
    }
}
