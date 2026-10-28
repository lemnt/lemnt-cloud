package com.lemnt.member.start;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.member.application.address.service.MemberAddressService;
import com.lemnt.member.application.dubbo.MemberUserClientImpl;
import com.lemnt.member.application.growth.executor.MemberAssetRecordAllPageQueryExe;
import com.lemnt.member.application.growth.executor.MemberAssetRecordDetailQueryExe;
import com.lemnt.member.application.growth.executor.MemberLevelRecordAllPageQueryExe;
import com.lemnt.member.application.growth.executor.MemberLevelRecordDetailQueryExe;
import com.lemnt.member.application.growth.executor.MemberSignRecordAllPageQueryExe;
import com.lemnt.member.application.growth.qry.AssetRecordAllPageQry;
import com.lemnt.member.application.growth.qry.AssetRecordDetailQry;
import com.lemnt.member.application.growth.qry.LevelRecordAllPageQry;
import com.lemnt.member.application.growth.qry.LevelRecordDetailQry;
import com.lemnt.member.application.growth.qry.SignRecordAllPageQry;
import com.lemnt.member.application.growth.service.MemberGrowthService;
import com.lemnt.member.application.user.cmd.PasswordChangeCmd;
import com.lemnt.member.application.user.executor.MemberGroupPageQueryExe;
import com.lemnt.member.application.user.executor.MemberPasswordChangeCmdExe;
import com.lemnt.member.application.user.executor.MemberTagPageQueryExe;
import com.lemnt.member.application.user.qry.GroupPageQry;
import com.lemnt.member.application.user.qry.TagPageQry;
import com.lemnt.member.application.user.service.MemberUserService;
import com.lemnt.member.domain.address.gateway.MemberAddressGateway;
import com.lemnt.member.domain.address.model.Address;
import com.lemnt.member.domain.growth.gateway.MemberAssetRecordGateway;
import com.lemnt.member.domain.growth.gateway.MemberConfigGateway;
import com.lemnt.member.domain.growth.gateway.MemberLevelGateway;
import com.lemnt.member.domain.growth.gateway.MemberLevelRecordGateway;
import com.lemnt.member.domain.growth.gateway.MemberSignConfigGateway;
import com.lemnt.member.domain.growth.gateway.MemberSignRecordGateway;
import com.lemnt.member.domain.growth.model.Level;
import com.lemnt.member.domain.growth.model.SignConfig;
import com.lemnt.member.domain.user.gateway.MemberGroupGateway;
import com.lemnt.member.domain.user.gateway.MemberTagGateway;
import com.lemnt.member.domain.user.gateway.MemberTagLinkGateway;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;
import com.lemnt.member.domain.user.model.Group;
import com.lemnt.member.domain.user.model.Member;
import com.lemnt.member.domain.user.model.Tag;
import com.lemnt.member.infrastructure.address.gateway.MemberAddressGatewayImpl;
import com.lemnt.member.infrastructure.address.mapper.MemberAddressMapper;
import com.lemnt.member.infrastructure.growth.gateway.MemberAssetRecordGatewayImpl;
import com.lemnt.member.infrastructure.growth.gateway.MemberConfigGatewayImpl;
import com.lemnt.member.infrastructure.growth.gateway.MemberLevelGatewayImpl;
import com.lemnt.member.infrastructure.growth.gateway.MemberLevelRecordGatewayImpl;
import com.lemnt.member.infrastructure.growth.gateway.MemberSignConfigGatewayImpl;
import com.lemnt.member.infrastructure.growth.gateway.MemberSignRecordGatewayImpl;
import com.lemnt.member.infrastructure.growth.mapper.MemberAssetRecordMapper;
import com.lemnt.member.infrastructure.growth.mapper.MemberConfigMapper;
import com.lemnt.member.infrastructure.growth.mapper.MemberLevelMapper;
import com.lemnt.member.infrastructure.growth.mapper.MemberLevelRecordMapper;
import com.lemnt.member.infrastructure.growth.mapper.MemberSignConfigMapper;
import com.lemnt.member.infrastructure.growth.mapper.MemberSignRecordMapper;
import com.lemnt.member.infrastructure.user.gateway.MemberGroupGatewayImpl;
import com.lemnt.member.infrastructure.user.gateway.MemberTagGatewayImpl;
import com.lemnt.member.infrastructure.user.gateway.MemberTagLinkGatewayImpl;
import com.lemnt.member.infrastructure.user.gateway.MemberUserGatewayImpl;
import com.lemnt.member.infrastructure.user.mapper.MemberGroupMapper;
import com.lemnt.member.infrastructure.user.mapper.MemberTagLinkMapper;
import com.lemnt.member.infrastructure.user.mapper.MemberTagMapper;
import com.lemnt.member.infrastructure.user.mapper.MemberUserMapper;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MemberAccountIntegrationTest {
    MemberUserGateway memberUserGateway;
    MemberGroupGateway memberGroupGateway;
    MemberTagGateway memberTagGateway;
    MemberTagLinkGateway memberTagLinkGateway;
    MemberAddressGateway memberAddressGateway;
    MemberAssetRecordGateway memberAssetRecordGateway;
    MemberLevelGateway memberLevelGateway;
    MemberLevelRecordGateway memberLevelRecordGateway;
    MemberSignConfigGateway memberSignConfigGateway;
    MemberSignRecordGateway memberSignRecordGateway;
    MemberConfigGateway memberConfigGateway;
    JdbcDataSource source;
    Connection schema;
    MemberUserService userService;
    MemberAddressService addressService;
    MemberGrowthService growthService;
    MemberPasswordChangeCmdExe passwordChange;
    Long id, other;
    MemberGroupPageQueryExe memberGroupPageQueryExe;
    MemberTagPageQueryExe memberTagPageQueryExe;
    MemberAssetRecordAllPageQueryExe memberAssetRecordAllPageQueryExe;
    MemberLevelRecordAllPageQueryExe memberLevelRecordAllPageQueryExe;
    MemberSignRecordAllPageQueryExe memberSignRecordAllPageQueryExe;
    MemberAssetRecordDetailQueryExe memberAssetRecordDetailQueryExe;
    MemberLevelRecordDetailQueryExe memberLevelRecordDetailQueryExe;

    @SuppressWarnings("unchecked")
    <T> T tx(T target) {
        var p = new ProxyFactory(target);
        p.addAdvice(
                new TransactionInterceptor(
                new DataSourceTransactionManager(source),
                new AnnotationTransactionAttributeSource()));
        return (T) p.getProxy();
    }

    /** 验证会员业务失败沿用统一异常类型和原有状态。 */
    private void assertBusinessException(int status, Executable operation) {
        var exception = assertThrows(ServiceException.class, operation);
        assertEquals(status, exception.getCode());
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
        var config = new MybatisConfiguration();
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        config.addInterceptor(interceptor);
        config.setMapUnderscoreToCamelCase(true);
        var bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(source);
        bean.setConfiguration(config);
        bean.setGlobalConfig(new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
        var factory = bean.getObject();
        config.addMapper(MemberAddressMapper.class);
        config.addMapper(MemberUserMapper.class);
        config.addMapper(MemberGroupMapper.class);
        config.addMapper(MemberTagMapper.class);
        config.addMapper(MemberTagLinkMapper.class);
        config.addMapper(MemberAssetRecordMapper.class);
        config.addMapper(MemberLevelMapper.class);
        config.addMapper(MemberLevelRecordMapper.class);
        config.addMapper(MemberSignConfigMapper.class);
        config.addMapper(MemberSignRecordMapper.class);
        config.addMapper(MemberConfigMapper.class);
        var template = new SqlSessionTemplate(factory);
        memberUserGateway = new MemberUserGatewayImpl(template.getMapper(MemberUserMapper.class));
        memberGroupGateway = new MemberGroupGatewayImpl(template.getMapper(MemberGroupMapper.class));
        memberTagGateway = new MemberTagGatewayImpl(template.getMapper(MemberTagMapper.class));
        memberTagLinkGateway = new MemberTagLinkGatewayImpl(template.getMapper(MemberTagLinkMapper.class));
        memberAddressGateway = new MemberAddressGatewayImpl(template.getMapper(MemberAddressMapper.class));
        memberAssetRecordGateway = new MemberAssetRecordGatewayImpl(template.getMapper(MemberAssetRecordMapper.class));
        memberLevelGateway = new MemberLevelGatewayImpl(template.getMapper(MemberLevelMapper.class));
        memberLevelRecordGateway = new MemberLevelRecordGatewayImpl(template.getMapper(MemberLevelRecordMapper.class));
        memberSignConfigGateway = new MemberSignConfigGatewayImpl(template.getMapper(MemberSignConfigMapper.class));
        memberSignRecordGateway = new MemberSignRecordGatewayImpl(template.getMapper(MemberSignRecordMapper.class));
        memberConfigGateway = new MemberConfigGatewayImpl(template.getMapper(MemberConfigMapper.class));
        userService = tx(new MemberUserService(memberGroupGateway, memberTagGateway, memberTagLinkGateway, memberUserGateway));
        addressService = tx(new MemberAddressService(memberUserGateway, memberAddressGateway));
        growthService = tx(new MemberGrowthService(memberAssetRecordGateway, memberConfigGateway, memberLevelGateway, memberLevelRecordGateway, memberSignConfigGateway, memberSignRecordGateway, memberUserGateway));
        passwordChange = tx(new MemberPasswordChangeCmdExe(memberUserGateway));
        memberGroupPageQueryExe = new MemberGroupPageQueryExe(memberGroupGateway);
        memberTagPageQueryExe = new MemberTagPageQueryExe(memberTagGateway);
        memberAssetRecordAllPageQueryExe = new MemberAssetRecordAllPageQueryExe(memberAssetRecordGateway);
        memberLevelRecordAllPageQueryExe = new MemberLevelRecordAllPageQueryExe(memberLevelRecordGateway);
        memberSignRecordAllPageQueryExe = new MemberSignRecordAllPageQueryExe(memberSignRecordGateway);
        memberAssetRecordDetailQueryExe = new MemberAssetRecordDetailQueryExe(memberAssetRecordGateway);
        memberLevelRecordDetailQueryExe = new MemberLevelRecordDetailQueryExe(memberLevelRecordGateway);
        id = memberUserGateway.save(new Member(null, "one", "13800000001", null, null, 1, 0L, 0L, null, null, null, null, null)).id();
        other = memberUserGateway.save(new Member(null, "two", "13800000002", null, null, 1, 0L, 0L, null, null, null, null, null)).id();
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

    @Test
    void memberClientKeepsItsReadOnlyProjectionAndMissingMemberContract() {
        var client = new MemberUserClientImpl(memberUserGateway, memberLevelGateway);
        var member = client.getByMemberId(id);
        assertEquals(id, member.id());
        assertEquals("one", member.nickname());
        assertEquals(1, member.status());
        userService.profileUpdate(id, "one", null, null, null, null, 0);
        assertEquals(0, client.getByMemberId(id).status());
        assertNull(client.getByMemberId(Long.MAX_VALUE));
    }

    @Test
    void memberPasswordDiagnosticsDoNotExposeHashes() {
        String hash = "sensitive-password-hash";
        var row = new com.lemnt.member.infrastructure.user.dataobject.MemberDO();
        row.setId(id);
        row.setPassword(hash);
        assertFalse(row.toString().contains(hash));
        assertFalse(memberUserGateway.findById(id).toString().contains(hash));
    }

    @Test
    void passwordMaintenanceUpdatesMemberUserAfterVerifyingOldPassword() {
        assertBusinessException(401,
                () -> passwordChange.execute(new PasswordChangeCmd(id, "missing-password", "New-password-2026")));
        memberUserGateway.updatePassword(id, com.lemnt.common.security.utils.SecurityUtils.encryptPassword("Member-password-2026"));
        assertTrue(SecurityUtils.matchesPassword("Member-password-2026", memberUserGateway.findPasswordByMemberId(id)));
        assertBusinessException(401,
                () -> passwordChange.execute(new PasswordChangeCmd(id, "wrong-password", "New-password-2026")));
        assertTrue(SecurityUtils.matchesPassword("Member-password-2026", memberUserGateway.findPasswordByMemberId(id)));
        passwordChange.execute(new PasswordChangeCmd(id, "Member-password-2026", "New-password-2026"));
        assertTrue(SecurityUtils.matchesPassword("New-password-2026", memberUserGateway.findPasswordByMemberId(id)));
        assertFalse(SecurityUtils.matchesPassword("Member-password-2026", memberUserGateway.findPasswordByMemberId(id)));
    }

    @Test
    void addressRpcOnlyReturnsTheOwnerAddressFromProductionGateway() {
        var saved = addressService.saveAddress(address(id, null, true));
        var client = new com.lemnt.member.application.dubbo.MemberAddressClientImpl(memberAddressGateway);
        assertEquals(id, client.get(id, saved.id()).memberId());
        assertEquals("测试地址", client.get(id, saved.id()).detailAddress());
        assertNull(client.get(other, saved.id()));
        assertNull(client.get(id, Long.MAX_VALUE));
        assertNull(client.get(null, saved.id()));
    }

    @Test
    void addressOwnershipAndDefaultAreEnforced() {
        var first = addressService.saveAddress(address(id, null, true));
        var second = addressService.saveAddress(address(id, null, true));
        assertFalse(memberAddressGateway.address(id, first.id()).defaultStatus());
        assertTrue(memberAddressGateway.address(id, second.id()).defaultStatus());
        assertNull(memberAddressGateway.address(other, first.id()));
        assertBusinessException(400,
                () -> addressService.saveAddress(address(other, first.id(), true)));
        assertFalse(addressService.deleteAddress(other, first.id()));
        assertEquals(2, memberAddressGateway.addresses(id).size());
    }

    @Test
    void concurrentDefaultsNeverLeaveTwoDefaultAddresses() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> addressService.saveAddress(address(id, null, true)));
            var b = pool.submit(() -> addressService.saveAddress(address(id, null, true)));
            a.get(15, TimeUnit.SECONDS);
            b.get(15, TimeUnit.SECONDS);
            assertEquals(1, memberAddressGateway.addresses(id).stream().filter(Address::defaultStatus).count());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void addressLimitIsCheckedBeforeClearingExistingDefault() {
        for (int n = 0; n < 20; n++) {
            addressService.saveAddress(address(id, null, n == 0));
        }
        assertBusinessException(409, () -> addressService.saveAddress(address(id, null, true)));
        assertEquals(20, memberAddressGateway.addresses(id).size());
        assertEquals(1, memberAddressGateway.addresses(id).stream().filter(Address::defaultStatus).count());
    }

    @Test
    void pointRequestIsIdempotentAndChangedContentCannotReplay() {
        var first = growthService.adjustAsset(id, "POINT", 10, "R1", "奖励");
        growthService.adjustAsset(id, "POINT", 2, "R2", "奖励2");
        assertEquals(first, growthService.adjustAsset(id, "POINT", 10, "R1", "奖励"));
        assertEquals(12, memberUserGateway.findById(id).points());
        assertBusinessException(409,
                () -> growthService.adjustAsset(id, "POINT", 11, "R1", "奖励"));
        assertBusinessException(400,
                () -> growthService.adjustAsset(id, "POINT", -13, "R3", "扣减"));
        assertEquals(12, memberUserGateway.findById(id).points());
    }

    @Test
    void concurrentPointRequestCreditsExactlyOnce() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> growthService.adjustAsset(id, "POINT", 10, "R1", "奖励"));
            var b = pool.submit(() -> growthService.adjustAsset(id, "POINT", 10, "R1", "奖励"));
            assertEquals(a.get(15, TimeUnit.SECONDS).id(), b.get(15, TimeUnit.SECONDS).id());
            assertEquals(10, memberUserGateway.findById(id).points());
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
                () -> growthService.adjustAsset(id, "POINT", 1, "R1", "overflow"));
        assertEquals(0, count("member_asset_record"));
        sql("UPDATE member_user SET points=0 WHERE id=" + id);
        sql("ALTER TABLE member_asset_record ADD CONSTRAINT injected_failure CHECK(delta<0)");
        assertThrows(Exception.class, () -> growthService.adjustAsset(id, "POINT", 1, "R2", "rollback"));
        assertEquals(0, memberUserGateway.findById(id).points());
    }

    @Test
    void levelThresholdsExperienceAndManualAssignmentHaveAuditAndReplay() {
        Long level = growthService.saveLevel(new Level(null, "Gold", 1, 100L, 90, null, null, 1));
        growthService.adjustAsset(id, "EXPERIENCE", 110, "E1", "升级");
        assertEquals(level, memberUserGateway.findById(id).levelId());
        assertEquals(1, memberLevelRecordGateway.levelRecordCount(id));
        assertBusinessException(400,
                () -> growthService.saveLevel(new Level(null, "Silver", 2, 50L, 95, null, null, 1)));
        assertFalse(growthService.deleteLevel(level));
        growthService.assignLevel(id, null, "L1", "取消等级");
        assertNull(memberUserGateway.findById(id).levelId());
        assertEquals(0, memberUserGateway.findById(id).experience());
        growthService.adjustAsset(id, "EXPERIENCE", 2, "E2", "after");
        growthService.assignLevel(id, null, "L1", "取消等级");
        assertEquals(2, memberUserGateway.findById(id).experience());
        assertBusinessException(409, () -> growthService.assignLevel(id, level, "L1", "取消等级"));
    }

    @Test
    void groupAndTagsEnforceReferencesAndOwnership() {
        Long group = userService.saveGroup(new Group(null, "VIP", null, 1));
        Long tag = userService.saveTag(new Tag(null, "active", null));
        userService.assignGroup(id, group);
        userService.assignTags(id, List.of(tag));
        assertFalse(userService.deleteGroup(group));
        assertFalse(userService.deleteTag(tag));
        assertEquals(List.of(tag), memberTagLinkGateway.memberTags(id));
        userService.assignTags(id, List.of());
        userService.assignGroup(id, null);
        assertTrue(userService.deleteTag(tag));
        assertTrue(userService.deleteGroup(group));
    }

    @Test
    void dailySignInIsIdempotentAndCreditsBothAssetsAtomically() throws Exception {
        growthService.saveSignConfig(new SignConfig(1, 10L, 2L, 1));
        var first = growthService.sign(id);
        assertEquals(first, growthService.sign(id));
        assertEquals(10, memberUserGateway.findById(id).points());
        assertEquals(2, memberUserGateway.findById(id).experience());
        assertEquals(1, count("member_sign_record"));
        assertEquals(2, count("member_asset_record"));
    }

    @Test
    void concurrentSignInAwardsOnce() throws Exception {
        growthService.saveSignConfig(new SignConfig(1, 10L, 2L, 1));
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> growthService.sign(id));
            var b = pool.submit(() -> growthService.sign(id));
            assertEquals(a.get(15, TimeUnit.SECONDS).id(), b.get(15, TimeUnit.SECONDS).id());
            assertEquals(10, memberUserGateway.findById(id).points());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void failedExperienceLedgerRollsBackSignAndPoints() throws Exception {
        growthService.saveSignConfig(new SignConfig(1, 10L, 2L, 1));
        sql(
                "ALTER TABLE member_asset_record ADD CONSTRAINT reject_experience"
                + " CHECK(asset_type!='EXPERIENCE')");
        assertThrows(Exception.class, () -> growthService.sign(id));
        assertEquals(0, count("member_sign_record"));
        assertEquals(0, memberUserGateway.findById(id).points());
        assertEquals(0, count("member_asset_record"));
    }

    @Test
    void profileValidationDoesNotAllowFutureBirthdayOrUnsafeAvatar() {
        assertBusinessException(400,
                () -> userService.profileUpdate(id, "x", null, "javascript:bad", null, null, null));
        assertBusinessException(400,
                () -> userService.profileUpdate(id, "x", null, null, 9, null, null));
        assertBusinessException(400,
                () ->
                userService.profileUpdate(
                id,
                "x",
                null,
                null,
                0,
                memberUserGateway.now().toLocalDate().plusDays(1),
                null));
        assertTrue(
                userService.profileUpdate(
                id,
                "new",
                "valid@example.com",
                "https://example.test/avatar",
                1,
                null,
                null));
        assertEquals("valid@example.com", memberUserGateway.findById(id).email());
        userService.profileUpdate(id, "new", null, null, 1, null, null);
        assertNull(memberUserGateway.findById(id).email());
        assertNull(memberUserGateway.findById(id).avatar());
    }

    @Test
    void adminRecordPagesAreBoundedFilteredAndTyped() {
        growthService.adjustAsset(id, "POINT", 5L, "test-record", "test");
        growthService.adjustAsset(other, "POINT", 7L, "test-record", "test");
        growthService.adjustAsset(id, "EXPERIENCE", 3L, "test-record", "test");
        var all =
        memberAssetRecordAllPageQueryExe.execute(
                new AssetRecordAllPageQry("POINT", null, 1, 1));
        assertEquals(2, all.getTotal());
        assertEquals(1, all.getRecords().size());
        var second = memberAssetRecordAllPageQueryExe.execute(
                new AssetRecordAllPageQry("POINT", null, 2, 1));
        assertEquals(2, second.getTotal());
        assertEquals(1, second.getRecords().size());
        assertNotEquals(all.getRecords().get(0).id(), second.getRecords().get(0).id());
        var beyond = memberAssetRecordAllPageQueryExe.execute(
                new AssetRecordAllPageQry("POINT", null, 3, 1));
        assertEquals(2, beyond.getTotal());
        assertTrue(beyond.getRecords().isEmpty());
        var mine =
        memberAssetRecordAllPageQueryExe.execute(
                new AssetRecordAllPageQry("POINT", id, 1, 20));
        assertEquals(1, mine.getTotal());
        assertEquals(id, mine.getRecords().get(0).memberId());
        assertNull(
                memberAssetRecordDetailQueryExe.execute(
                new AssetRecordDetailQry(
                "EXPERIENCE", mine.getRecords().get(0).id())));
        assertBusinessException(400,
                () ->
                memberAssetRecordAllPageQueryExe.execute(
                new AssetRecordAllPageQry("POINT", null, 1, 101)));
        assertEquals(
                0,
                memberGroupPageQueryExe.execute(
                new GroupPageQry("absent", null, 1, 20))
                .getTotal());
    }

    @Test
    void allAdminMetadataAndRecordQueriesUseActualProductionSql() {
        var group = userService.saveGroup(new Group(null, "VIP", "remark", 1));
        userService.saveGroup(new Group(null, "inactive", null, 0));
        userService.saveTag(new Tag(null, "premium", null));
        var page =
        new GroupPageQry("VIP", 1, 1, 20);
        assertEquals(1, memberGroupPageQueryExe.execute(page).getTotal());
        assertEquals(group, memberGroupPageQueryExe.execute(page).getRecords().get(0).id());
        assertEquals(
                1,
                memberTagPageQueryExe.execute(
                new TagPageQry("premium", 1, 20))
                .getTotal());
        var level = growthService.saveLevel(new Level(null, "bronze", 1, 10L, 95, null, null, 1));
        growthService.assignLevel(id, level, "level-record-test", "test");
        var levels =
        memberLevelRecordAllPageQueryExe.execute(
                new LevelRecordAllPageQry(id, 1, 20));
        assertEquals(1, levels.getTotal());
        assertEquals(
                id,
                memberLevelRecordDetailQueryExe.execute(
                new LevelRecordDetailQry(levels.getRecords().get(0).id()))
                .memberId());
        growthService.saveSignConfig(new SignConfig(1, 1L, 1L, 1));
        growthService.sign(id);
        assertEquals(
                1,
                memberSignRecordAllPageQueryExe.execute(
                new SignRecordAllPageQry(null, 1, 20))
                .getTotal());
        assertEquals(
                0,
                memberSignRecordAllPageQueryExe.execute(
                new SignRecordAllPageQry(other, 1, 20))
                .getTotal());
    }

    /** 数据库读取留在会员服务，RPC 结果不在诊断文本泄露密码。 */
    @Test
    void memberUserClientReadsExistingStorageAndTracksCurrentStatus() {
        memberUserGateway.updatePassword(id, com.lemnt.common.security.utils.SecurityUtils.encryptPassword("Member-password-2026"));
        com.lemnt.member.client.MemberUserClient client = tx(
                (com.lemnt.member.client.MemberUserClient) new com.lemnt.member.application.dubbo.MemberUserClientImpl(
                        memberUserGateway, memberLevelGateway));
        var login = client.getByMobile("13800000001");
        assertEquals(id, login.memberId());
        assertTrue(SecurityUtils.matchesPassword("Member-password-2026", login.passwordHash()));
        assertFalse(login.toString().contains(login.passwordHash()));
        assertNull(client.getByMobile("13800000999"));
        assertEquals(1, client.getByMemberId(id).status());
        userService.profileUpdate(id, "one", null, null, null, null, 0);
        assertEquals(0, client.getByMemberId(id).status());
        assertNull(client.getByMemberId(Long.MAX_VALUE));
    }
    /** 下单抵扣、支付赠送、分次退款回退都使用原订单事实，回调重复不改变资产。 */
    @Test
    void tradeAssetsAreIdempotentAndRefundReturnsOnlyOriginalOrderBenefits() {
        growthService.adjustAsset(id, "POINT", 100, "SEED", "初始化");
        var create = new com.lemnt.member.application.growth.cmd.MemberTradeAssetCmd(
                id, "CREATE", "ORDER", null, 20, 10, 1000, 0, false);
        growthService.tradeAsset(create);
        growthService.tradeAsset(create);
        assertEquals(80L, memberUserGateway.findById(id).points());
        var paid = new com.lemnt.member.application.growth.cmd.MemberTradeAssetCmd(
                id, "PAID", "ORDER", null, 20, 10, 1000, 0, false);
        growthService.tradeAsset(paid);
        growthService.tradeAsset(paid);
        assertEquals(90L, memberUserGateway.findById(id).points());
        assertEquals(1000L, memberUserGateway.findById(id).experience());
        var first = new com.lemnt.member.application.growth.cmd.MemberTradeAssetCmd(
                id, "REFUND", "ORDER", "FIRST", 20, 10, 1000, 500, false);
        growthService.tradeAsset(first);
        growthService.tradeAsset(first);
        assertEquals(95L, memberUserGateway.findById(id).points());
        assertEquals(500L, memberUserGateway.findById(id).experience());
        growthService.tradeAsset(new com.lemnt.member.application.growth.cmd.MemberTradeAssetCmd(
                id, "REFUND", "ORDER", "LAST", 20, 10, 1000, 500, true));
        assertEquals(100L, memberUserGateway.findById(id).points());
        assertEquals(0L, memberUserGateway.findById(id).experience());
    }

    /** 未成功扣减不能通过关单获得积分，终态会拒绝迟到的抵扣。 */
    @Test
    void closedTradeDoesNotCreditUndeductedPointsAndRejectsLateCreate() {
        var create = new com.lemnt.member.application.growth.cmd.MemberTradeAssetCmd(
                id, "CREATE", "CLOSED", null, 20, 0, 1000, 0, false);
        assertThrows(ServiceException.class, () -> growthService.tradeAsset(create));
        growthService.tradeAsset(new com.lemnt.member.application.growth.cmd.MemberTradeAssetCmd(
                id, "CLOSED", "CLOSED", null, 20, 0, 1000, 0, false));
        assertEquals(0L, memberUserGateway.findById(id).points());
        growthService.adjustAsset(id, "POINT", 100, "SEED", "初始化");
        assertThrows(ServiceException.class, () -> growthService.tradeAsset(create));
        assertEquals(100L, memberUserGateway.findById(id).points());
    }

    /** 赠送积分已使用时不产生负余额，重复退款也不扣除后来获得的积分。 */
    @Test
    void refundCannotCreateNegativeBalanceOrReclaimFutureUnrelatedPoints() {
        growthService.tradeAsset(new com.lemnt.member.application.growth.cmd.MemberTradeAssetCmd(
                id, "PAID", "SPENT", null, 0, 10, 1000, 0, false));
        growthService.adjustAsset(id, "POINT", -10, "SPEND", "使用积分");
        var refund = new com.lemnt.member.application.growth.cmd.MemberTradeAssetCmd(
                id, "REFUND", "SPENT", "REFUND", 0, 10, 1000, 1000, true);
        growthService.tradeAsset(refund);
        assertEquals(0L, memberUserGateway.findById(id).points());
        growthService.adjustAsset(id, "POINT", 5, "OTHER", "其他业务奖励");
        growthService.tradeAsset(refund);
        assertEquals(5L, memberUserGateway.findById(id).points());
    }
}
