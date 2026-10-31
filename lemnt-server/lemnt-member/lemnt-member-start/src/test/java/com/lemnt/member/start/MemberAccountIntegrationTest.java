package com.lemnt.member.start;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.member.application.address.service.MemberAddressService;
import com.lemnt.member.application.dubbo.MemberUserClientImpl;
import com.lemnt.member.application.growth.executor.MemberAssetRecordAllPageQueryExe;
import com.lemnt.member.application.growth.executor.MemberAssetRecordDetailQueryExe;
import com.lemnt.member.application.growth.executor.MemberLevelRecordAllPageQueryExe;
import com.lemnt.member.application.growth.executor.MemberExperienceRecordAllPageQueryExe;
import com.lemnt.member.application.growth.qry.ExperienceRecordAllPageQry;
import java.time.LocalDateTime;
import com.lemnt.member.application.growth.executor.MemberLevelRecordDetailQueryExe;
import com.lemnt.member.application.growth.executor.MemberSignRecordAllPageQueryExe;
import com.lemnt.member.application.growth.qry.AssetRecordAllPageQry;
import com.lemnt.member.application.growth.qry.AssetRecordDetailQry;
import com.lemnt.member.application.growth.qry.SignRecordAllPageQry;
import com.lemnt.member.domain.growth.model.SignRecord;
import com.lemnt.member.application.growth.qry.AssetRecordPageQry;
import com.lemnt.member.application.growth.executor.MemberAssetRecordPageQueryExe;
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
import com.lemnt.member.domain.growth.gateway.MemberPointRecordGateway;
import com.lemnt.member.domain.growth.gateway.MemberExperienceRecordGateway;
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
import com.lemnt.member.infrastructure.growth.gateway.MemberConfigGatewayImpl;
import com.lemnt.member.infrastructure.growth.gateway.MemberLevelGatewayImpl;
import com.lemnt.member.infrastructure.growth.gateway.MemberLevelRecordGatewayImpl;
import com.lemnt.member.infrastructure.growth.gateway.MemberSignConfigGatewayImpl;
import com.lemnt.member.infrastructure.growth.gateway.MemberSignRecordGatewayImpl;
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
    MemberPointRecordGateway pointRecords;
    MemberExperienceRecordGateway experienceRecords;
    MemberLevelGateway memberLevelGateway;
    MemberLevelRecordGateway memberLevelRecordGateway;
    MemberSignConfigGateway memberSignConfigGateway;
    MemberSignRecordGateway memberSignRecordGateway;
    MemberConfigGateway memberConfigGateway;
    JdbcDataSource source;
    Connection schema;
    MemberUserService userService;
    com.lemnt.member.domain.user.gateway.MemberAuthGateway memberAuthGateway;
    MemberAddressService addressService;
    SqlSessionTemplate template;
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
        ScriptUtils.executeSqlScript(schema,new ClassPathResource("member-sql/V4__member_point_record.sql"));
        ScriptUtils.executeSqlScript(schema,new ClassPathResource("member-sql/V5__member_experience_record.sql"));
        var config = new MybatisConfiguration();
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        config.addInterceptor(interceptor);
        config.setMapUnderscoreToCamelCase(true);
        var bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(source);
        bean.setConfiguration(config);
        bean.setMapperLocations(new org.springframework.core.io.support.PathMatchingResourcePatternResolver().getResources("classpath*:mapper/Member*Mapper.xml"));
        bean.setGlobalConfig(new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
        var factory = bean.getObject();
        config.addMapper(MemberAddressMapper.class);
        if (!config.hasMapper(MemberUserMapper.class)) config.addMapper(MemberUserMapper.class);
        if (!config.hasMapper(MemberGroupMapper.class)) config.addMapper(MemberGroupMapper.class);
        if (!config.hasMapper(MemberTagMapper.class)) config.addMapper(MemberTagMapper.class);
        config.addMapper(MemberTagLinkMapper.class);
        if (!config.hasMapper(MemberLevelMapper.class)) config.addMapper(MemberLevelMapper.class);
        config.addMapper(MemberLevelRecordMapper.class);
        config.addMapper(MemberSignConfigMapper.class);
        if (!config.hasMapper(MemberSignRecordMapper.class)) config.addMapper(MemberSignRecordMapper.class);
        config.addMapper(MemberConfigMapper.class);
        template = new SqlSessionTemplate(factory);
        memberUserGateway = new MemberUserGatewayImpl(template.getMapper(MemberUserMapper.class));
        memberGroupGateway = new MemberGroupGatewayImpl(template.getMapper(MemberGroupMapper.class));
        memberTagGateway = new MemberTagGatewayImpl(template.getMapper(MemberTagMapper.class));
        memberTagLinkGateway = new MemberTagLinkGatewayImpl(template.getMapper(MemberTagLinkMapper.class));
        memberAddressGateway = new MemberAddressGatewayImpl(template.getMapper(MemberAddressMapper.class));
        pointRecords=new com.lemnt.member.infrastructure.growth.gateway.MemberPointRecordGatewayImpl(template.getMapper(com.lemnt.member.infrastructure.growth.mapper.MemberPointRecordMapper.class));
        experienceRecords=new com.lemnt.member.infrastructure.growth.gateway.MemberExperienceRecordGatewayImpl(template.getMapper(com.lemnt.member.infrastructure.growth.mapper.MemberExperienceRecordMapper.class));
        memberLevelGateway = new MemberLevelGatewayImpl(template.getMapper(MemberLevelMapper.class));
        memberLevelRecordGateway = new MemberLevelRecordGatewayImpl(template.getMapper(MemberLevelRecordMapper.class));
        memberSignConfigGateway = new MemberSignConfigGatewayImpl(template.getMapper(MemberSignConfigMapper.class));
        memberSignRecordGateway = new MemberSignRecordGatewayImpl(template.getMapper(MemberSignRecordMapper.class));
        memberConfigGateway = new MemberConfigGatewayImpl(template.getMapper(MemberConfigMapper.class));
        memberAuthGateway = org.mockito.Mockito.mock(com.lemnt.member.domain.user.gateway.MemberAuthGateway.class);
        userService = tx(new MemberUserService(memberGroupGateway, memberTagGateway, memberTagLinkGateway, memberUserGateway, memberAuthGateway));
        addressService = tx(new MemberAddressService(memberUserGateway, memberAddressGateway));
        growthService = tx(new MemberGrowthService(pointRecords,experienceRecords, memberConfigGateway, new MemberConfigLockTestSupport().gateway, memberLevelGateway, memberLevelRecordGateway, memberSignConfigGateway, memberSignRecordGateway, memberUserGateway));
        passwordChange = tx(new MemberPasswordChangeCmdExe(memberUserGateway));
        memberGroupPageQueryExe = new MemberGroupPageQueryExe(memberGroupGateway);
        memberTagPageQueryExe = new MemberTagPageQueryExe(memberTagGateway);
        memberAssetRecordAllPageQueryExe = new MemberAssetRecordAllPageQueryExe(pointRecords,experienceRecords);
        memberLevelRecordAllPageQueryExe = new MemberLevelRecordAllPageQueryExe(memberLevelRecordGateway);
        memberSignRecordAllPageQueryExe = new MemberSignRecordAllPageQueryExe(memberSignRecordGateway);
        memberAssetRecordDetailQueryExe = new MemberAssetRecordDetailQueryExe(pointRecords,experienceRecords);
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
        assertBusinessException(404,
                () -> addressService.saveAddress(address(other, first.id(), true)));
        assertBusinessException(404, () -> addressService.deleteAddress(other, first.id()));
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
    void addressCreationHasNoAdditionalTwentyItemLimit() {
        for (int n = 0; n < 20; n++) {
            addressService.saveAddress(address(id, null, n == 0));
        }
        var newest = addressService.saveAddress(address(id, null, true));
        assertEquals(21, memberAddressGateway.addresses(id).size());
        assertEquals(newest, memberAddressGateway.defaultAddress(id));
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
            assertEquals(1, count("member_point_record"));
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
        assertEquals(0, count("member_point_record"));
        sql("UPDATE member_user SET points=0 WHERE id=" + id);
        sql("ALTER TABLE member_point_record ADD CONSTRAINT injected_failure CHECK(point<0)");
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
        assertBusinessException(409, () -> growthService.deleteLevel(level));
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
        assertBusinessException(409, () -> userService.deleteGroup(group));
        assertBusinessException(409, () -> userService.deleteTag(tag));
        assertEquals(List.of(tag), memberTagLinkGateway.memberTags(id));
        userService.assignTags(id, List.of());
        userService.assignGroup(id, null);
        assertTrue(userService.deleteTag(tag));
        assertTrue(userService.deleteGroup(group));
    }

    @Test
    void dailySignInRejectsDuplicatesAndCreditsBothAssetsAtomically() throws Exception {
        growthService.createSignConfig(new SignConfig(1, 10L, 2L, 1));
        var first = growthService.sign(id);
        assertBusinessException(409, () -> growthService.sign(id));
        assertEquals(10, memberUserGateway.findById(id).points());
        assertEquals(2, memberUserGateway.findById(id).experience());
        assertEquals(1, count("member_sign_in_record"));
        assertEquals(1, count("member_experience_record"));
        assertEquals(1, count("member_point_record"));
        assertNotNull(pointRecords.request(id, "SIGN_IN", first.id().toString()));
        assertNotNull(experienceRecords.request(id, "SIGN_IN", first.id().toString()));
        assertEquals(first, memberSignRecordGateway.signDate(id, first.signDate()));
    }

    @Test
    void concurrentSignInAwardsOnce() throws Exception {
        growthService.createSignConfig(new SignConfig(1, 10L, 2L, 1));
        var pool = Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Callable<Object> attempt = () -> {
                try {
                    return growthService.sign(id);
                } catch (ServiceException exception) {
                    return exception;
                }
            };
            var a = pool.submit(attempt);
            var b = pool.submit(attempt);
            var results = List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
            assertEquals(1, results.stream().filter(SignRecord.class::isInstance).count());
            var rejected = results.stream().filter(ServiceException.class::isInstance)
                    .map(ServiceException.class::cast).toList();
            assertEquals(1, rejected.size());
            assertEquals(409, rejected.get(0).getCode());
            assertEquals(10, memberUserGateway.findById(id).points());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void failedExperienceLedgerRollsBackSignAndPoints() throws Exception {
        growthService.createSignConfig(new SignConfig(1, 10L, 2L, 1));
        sql(
                "ALTER TABLE member_experience_record ADD CONSTRAINT reject_experience"
                + " CHECK(experience<0)");
        assertThrows(Exception.class, () -> growthService.sign(id));
        assertEquals(0, count("member_point_record"));
        assertEquals(0, count("member_sign_in_record"));
        assertEquals(0, memberUserGateway.findById(id).points());
        assertEquals(0, count("member_experience_record"));
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
        var experienceDetail=memberAssetRecordDetailQueryExe.execute(new AssetRecordDetailQry("EXPERIENCE",mine.getRecords().get(0).id()));
        assertNotNull(experienceDetail);assertEquals("EXPERIENCE",experienceDetail.assetType());assertEquals(3L,experienceDetail.delta());
        var pointDetail=memberAssetRecordDetailQueryExe.execute(new AssetRecordDetailQry("POINT",mine.getRecords().get(0).id()));
        assertEquals("POINT",pointDetail.assetType());assertEquals(mine.getRecords().get(0).delta(),pointDetail.delta());
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
        growthService.createSignConfig(new SignConfig(1, 1L, 1L, 1));
        growthService.sign(id);
        assertEquals(
                1,
                memberSignRecordAllPageQueryExe.execute(
                new com.lemnt.member.application.growth.qry.SignRecordAllPageQry(null, 1, 20))
                .getTotal());
        assertEquals(
                0,
                memberSignRecordAllPageQueryExe.execute(
                new com.lemnt.member.application.growth.qry.SignRecordAllPageQry(other, 1, 20))
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
    @org.junit.jupiter.api.Test
    void internalMarkPersistsClearsAndIsExcludedFromMemberContracts() {
        userService.profileUpdate(id, "one", null, null, null, null, null, "仅后台可见");
        assertEquals("仅后台可见", memberUserGateway.findById(id).mark());
        userService.profileUpdate(id, "new", null, null, null, null, null);
        assertEquals("仅后台可见", memberUserGateway.findById(id).mark());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> userService.profileUpdate(id, "new", null, null, null, null, null, "字".repeat(256)));
        assertEquals("仅后台可见", memberUserGateway.findById(id).mark());
        userService.profileUpdate(id, "new", null, null, null, null, null, "");
        assertEquals("", memberUserGateway.findById(id).mark());
        for (Class<?> contract : java.util.List.of(
                com.lemnt.member.adapter.app.user.req.ProfileUpdateReq.class,
                com.lemnt.member.adapter.app.user.res.ProfileRes.class)) {
            assertFalse(java.util.Arrays.stream(contract.getRecordComponents()).anyMatch(v -> v.getName().equals("mark")));
        }
    }

    @org.junit.jupiter.api.Test
    void backendIdentityFieldsPreserveOnMemberUpdateAndValidateBeforeWrite() {
        userService.profileUpdate(id, "one", null, null, null, null, null, null, "真实姓名", 110101);
        assertEquals("真实姓名", memberUserGateway.findById(id).name());
        assertEquals(110101, memberUserGateway.findById(id).areaId());
        userService.profileUpdate(id, "本人修改昵称", null, null, null, null, null);
        assertEquals("真实姓名", memberUserGateway.findById(id).name());
        assertEquals(110101, memberUserGateway.findById(id).areaId());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> userService.profileUpdate(id, "不得写入", null, null, null, null, null, null, "字".repeat(31), 110101));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> userService.profileUpdate(id, "不得写入", null, null, null, null, null, null, "姓名", 0));
        assertEquals("本人修改昵称", memberUserGateway.findById(id).nickname());
        userService.profileUpdate(id, "one", null, null, null, null, null, null, "", null);
        assertEquals("", memberUserGateway.findById(id).name());
        assertEquals(110101, memberUserGateway.findById(id).areaId());
        for (Class<?> contract : java.util.List.of(com.lemnt.member.adapter.app.user.req.ProfileUpdateReq.class,
                com.lemnt.member.adapter.app.user.res.ProfileRes.class)) {
            assertFalse(java.util.Arrays.stream(contract.getRecordComponents()).anyMatch(v -> v.getName().equals("name") || v.getName().equals("areaId")));
        }
    }

    @org.junit.jupiter.api.Test
    void loginTimeUsesMemberDatabaseAndOrdinaryProfileUpdateCannotClearIt() {
        com.lemnt.member.client.MemberUserClient client=tx((com.lemnt.member.client.MemberUserClient) new com.lemnt.member.application.dubbo.MemberUserClientImpl(memberUserGateway,memberLevelGateway));
        assertNull(memberUserGateway.findById(id).loginDate());
        var before=memberUserGateway.now();
        client.recordLogin(id);
        var logged=memberUserGateway.findById(id).loginDate();
        assertNotNull(logged);assertFalse(logged.isBefore(before.withNano(0)));
        userService.profileUpdate(id,"one",null,null,null,null,null);
        assertEquals(logged,memberUserGateway.findById(id).loginDate());
        userService.profileUpdate(id,"one",null,null,null,null,0);
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,()->client.recordLogin(id));
        assertEquals(logged,memberUserGateway.findById(id).loginDate());
    }

    @org.junit.jupiter.api.Test
    void loginIpAndTimeAreSavedTogetherAndUnknownDoesNotReusePreviousIp() {
        com.lemnt.member.client.MemberUserClient client=tx((com.lemnt.member.client.MemberUserClient)new com.lemnt.member.application.dubbo.MemberUserClientImpl(memberUserGateway,memberLevelGateway));
        client.recordLogin(id,"203.0.113.7");
        assertEquals("203.0.113.7",memberUserGateway.findById(id).loginIp());
        assertNotNull(memberUserGateway.findById(id).loginDate());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,()->client.recordLogin(id,"example.com"));
        assertEquals("203.0.113.7",memberUserGateway.findById(id).loginIp());
        client.recordLogin(id,null);
        assertNull(memberUserGateway.findById(id).loginIp());
    }

    @org.junit.jupiter.api.Test
    void signRecordNicknameFilterAndPageTotalUseSameRealJoinedRecords() {
        var today=memberUserGateway.now().toLocalDate();
        memberSignRecordGateway.signSave(new com.lemnt.member.domain.growth.model.SignRecord(null,id,today,1,1L,0L));
        memberSignRecordGateway.signSave(new com.lemnt.member.domain.growth.model.SignRecord(null,other,today,1,2L,0L));
        var query=new com.lemnt.member.application.growth.executor.MemberSignRecordAllPageQueryExe(memberSignRecordGateway);
        var page=query.execute(new com.lemnt.member.application.growth.qry.SignRecordAllPageQry(null,1,1,"o"));
        assertEquals(2,page.getTotal());assertEquals(1,page.getRecords().size());
        assertEquals("two",page.getRecords().get(0).nickname());
        assertEquals(1,query.execute(new com.lemnt.member.application.growth.qry.SignRecordAllPageQry(id,1,20,"one")).getTotal());
        assertEquals(0,query.execute(new com.lemnt.member.application.growth.qry.SignRecordAllPageQry(id,1,20,"two")).getTotal());
        assertEquals(0,query.execute(new com.lemnt.member.application.growth.qry.SignRecordAllPageQry(null,1,20,"无匹配")).getTotal());
        userService.profileUpdate(id,"改名",null,null,null,null,null);
        var renamed=query.execute(new com.lemnt.member.application.growth.qry.SignRecordAllPageQry(null,1,20,"改名"));
        assertEquals(1,renamed.getTotal());assertEquals("改名",renamed.getRecords().get(0).nickname());
    }

    @org.junit.jupiter.api.Test
    void signNicknameHttpBindingUsesIndependentCriterionAndReturnsCurrentNickname() throws Exception {
        memberSignRecordGateway.signSave(new com.lemnt.member.domain.growth.model.SignRecord(null,id,memberUserGateway.now().toLocalDate(),1,1L,0L));
        var query=new com.lemnt.member.application.growth.executor.MemberSignRecordAllPageQueryExe(memberSignRecordGateway);
        var mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(
                new com.lemnt.member.adapter.web.growth.MemberSignRecordController(query))
                .setControllerAdvice(new com.lemnt.common.security.handler.GlobalExceptionHandler()).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/member/admin/signin-records").param("nickname","one"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.total").value(1))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.rows[0].nickname").value("one"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/member/admin/signin-records").param("nickname","字".repeat(81)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is(422));
    }

    @org.junit.jupiter.api.Test
    void pointNicknameBusinessTypeAndMemberFilterShareCountAndExcludeExperience() {
        var now=memberUserGateway.now();
        pointRecords.save(new com.lemnt.member.domain.growth.model.AssetRecord(null,id,"POINT",1L,1L,"ADMIN","F1","调整",now));
        pointRecords.save(new com.lemnt.member.domain.growth.model.AssetRecord(null,other,"POINT",2L,2L,"SIGN_IN","F2","签到",now));
        experienceRecords.save(new com.lemnt.member.domain.growth.model.AssetRecord(null,id,"EXPERIENCE",3L,3L,"ADMIN","F3","经验",now));
        var query=new com.lemnt.member.application.growth.executor.MemberAssetRecordAllPageQueryExe(pointRecords,experienceRecords);
        var all=query.execute(new com.lemnt.member.application.growth.qry.AssetRecordAllPageQry("POINT",null,1,1,"o",null));
        assertEquals(2,all.getTotal());assertEquals(1,all.getRecords().size());assertEquals("two",all.getRecords().get(0).nickname());
        var filtered=query.execute(new com.lemnt.member.application.growth.qry.AssetRecordAllPageQry("POINT",id,1,20,"one","ADMIN"));
        assertEquals(1,filtered.getTotal());assertEquals("POINT",filtered.getRecords().get(0).assetType());
        assertEquals(0,query.execute(new com.lemnt.member.application.growth.qry.AssetRecordAllPageQry("POINT",id,1,20,"one","SIGN_IN")).getTotal());
        assertEquals(0,query.execute(new com.lemnt.member.application.growth.qry.AssetRecordAllPageQry("POINT",null,1,20,"无匹配",null)).getTotal());
        assertEquals(0L,memberUserGateway.findById(id).points());
    }

    @org.junit.jupiter.api.Test
    void pointFilterHttpBindsNicknameAndBusinessTypeAndRejectsOversize() throws Exception {
        pointRecords.save(new com.lemnt.member.domain.growth.model.AssetRecord(null,id,"POINT",1L,1L,"ADMIN","H1","调整",memberUserGateway.now()));
        var mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(
                new com.lemnt.member.adapter.web.growth.MemberPointRecordController(
                        new com.lemnt.member.application.growth.executor.MemberAssetRecordAllPageQueryExe(pointRecords,experienceRecords),
                        new com.lemnt.member.application.growth.executor.MemberAssetRecordDetailQueryExe(pointRecords,experienceRecords)))
                .setControllerAdvice(new com.lemnt.common.security.handler.GlobalExceptionHandler()).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/member/admin/point-records")
                .param("nickname","one").param("bizType","ADMIN").param("memberId",id.toString()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.total").value(1))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.rows[0].nickname").value("one"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/member/admin/point-records")
                .param("bizType","字".repeat(41))).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is(422));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/member/admin/point-records")
                .param("pointBizType","2")).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.total").value(0));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/member/admin/point-records")
                .param("pointBizType","3")).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/member/admin/point-records")
                .param("pointBizType","invalid")).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is(422));

    }

    @org.junit.jupiter.api.Test
    void pointMetadataIsSavedOnceAndReasonRemainsIndependent() {
        var first=growthService.adjustAsset(id,"POINT",10,"META-1","人工原因");
        assertEquals(2,first.pointBizType());assertEquals("管理员修改",first.title());
        assertEquals("管理员修改 10 积分",first.description());assertEquals("人工原因",first.reason());
        var repeated=growthService.adjustAsset(id,"POINT",10,"META-1","人工原因");
        assertEquals(first,repeated);assertEquals(10L,memberUserGateway.findById(id).points());
        var experience=growthService.adjustAsset(id,"EXPERIENCE",3,"META-EXP","经验原因");
        assertNull(experience.pointBizType());assertEquals(0,experience.experienceBizType());assertEquals("管理员调整",experience.title());
        var read=pointRecords.detail(first.id());
        assertEquals(first.title(),read.title());assertEquals(first.description(),read.description());
    }
    @org.junit.jupiter.api.Test
    void sourcePointEnumMappingDoesNotInventUnknownOrInternalMarkerMetadata() {
        var codes=java.util.Map.of("SIGN_IN",1,"ADMIN",2,"TRADE_USE",11,"TRADE_USE_CANCEL",12,"TRADE_USE_RETURN",13,"TRADE_GIVE",21,"TRADE_GIVE_RETURN",23);
        codes.forEach((code,number)->assertEquals(number,com.lemnt.member.domain.growth.model.PointRecordMetadata.from("POINT",code,10).type()));
        assertNull(com.lemnt.member.domain.growth.model.PointRecordMetadata.from("POINT","TRADE_CLOSED",0).type());
        assertNull(com.lemnt.member.domain.growth.model.PointRecordMetadata.from("EXPERIENCE","ADMIN",10).type());
        assertNull(com.lemnt.member.domain.growth.model.PointRecordMetadata.from("POINT","UNKNOWN",10).type());
    }

    @org.junit.jupiter.api.Test
    void numericPointFilterMatchesSavedMetadataAndKeepsLegacyUnknownOnlyWhenUnfiltered() {
        growthService.adjustAsset(id,"POINT",10,"NUMERIC-1","新流水");
        pointRecords.save(new com.lemnt.member.domain.growth.model.AssetRecord(null,id,"POINT",1L,11L,"ADMIN","LEGACY","旧流水",memberUserGateway.now()));
        var query=new com.lemnt.member.application.growth.executor.MemberAssetRecordAllPageQueryExe(pointRecords,experienceRecords);
        assertEquals(2,query.execute(new com.lemnt.member.application.growth.qry.AssetRecordAllPageQry("POINT",id,1,20)).getTotal());
        var result=query.execute(new com.lemnt.member.application.growth.qry.AssetRecordAllPageQry("POINT",id,1,1,"one","ADMIN",2));
        assertEquals(1,result.getTotal());assertEquals(1,result.getRecords().size());assertEquals(2,result.getRecords().get(0).pointBizType());
        assertEquals(0,query.execute(new com.lemnt.member.application.growth.qry.AssetRecordAllPageQry("POINT",id,1,20,null,"SIGN_IN",2)).getTotal());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,()->query.execute(new com.lemnt.member.application.growth.qry.AssetRecordAllPageQry("POINT",id,1,20,null,null,3)));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,()->query.execute(new com.lemnt.member.application.growth.qry.AssetRecordAllPageQry("EXPERIENCE",id,1,20,null,null,2)));
        assertEquals(0,query.execute(new com.lemnt.member.application.growth.qry.AssetRecordAllPageQry("POINT",id,1,20,null,null,22)).getTotal());
    }

    @org.junit.jupiter.api.Test
    void legacyPointCopyPreservesIdsUnknownMetadataAndOriginalRequestReplay() throws Exception {
        // 模拟未拆表旧库，保留源POINT归档；新库检查约束不允许此写入。
        createLegacyAssetTable();
        sql("INSERT INTO member_asset_record(id,member_id,asset_type,delta,balance,biz_type,biz_no,reason,create_time) VALUES(900,"+id+",'POINT',7,7,'ADMIN','OLD-POINT','旧原因',CURRENT_TIMESTAMP)");
        sql("UPDATE member_user SET points=7 WHERE id="+id);
        ScriptUtils.executeSqlScript(schema,new ClassPathResource("member-sql/upgrade/point_record_split.sql"));
        var copied=pointRecords.request(id,"ADMIN","OLD-POINT");
        var copiedAudit=template.getMapper(com.lemnt.member.infrastructure.growth.mapper.MemberPointRecordMapper.class).selectById(copied.id());
        assertNull(copiedAudit.getCreateBy());assertNull(copiedAudit.getUpdateBy());assertNull(copiedAudit.getUpdateTime());
        assertEquals(900L,copied.id());assertNull(experienceRecords.detail(copied.id()));assertNull(copied.pointBizType());assertNull(copied.title());assertEquals("旧原因",copied.reason());
        assertEquals(1,pointRecords.count(new com.lemnt.member.domain.growth.model.AssetRecordPageFilter(id,"POINT",0,1)));assertEquals(1,count("member_asset_record"));
        assertEquals(copied,growthService.adjustAsset(id,"POINT",7,"OLD-POINT","旧原因"));
        assertEquals(7L,memberUserGateway.findById(id).points());assertEquals(1,count("member_point_record"));
    }

    /** 仅迁移测试构造旧库表，新库初始化不保留泛化流水表。 */
    private void createLegacyAssetTable() throws Exception {
        sql("CREATE TABLE member_asset_record(id BIGINT PRIMARY KEY,member_id BIGINT,asset_type VARCHAR(16),delta BIGINT,balance BIGINT,biz_type VARCHAR(40),biz_no VARCHAR(64),reason VARCHAR(500),create_time DATETIME,point_biz_type INT,title VARCHAR(255),description VARCHAR(500))");
    }

    @org.junit.jupiter.api.Test
    void experienceHasOwnSourceMetadataAndRetryDoesNotRepeatGrowth() {
        var first=growthService.adjustAsset(id,"EXPERIENCE",5,"EXP-META","经验原因");
        assertNull(first.pointBizType());assertEquals(0,first.experienceBizType());assertEquals("管理员调整",first.title());
        assertEquals("管理员调整获得 5 经验",first.description());assertEquals("经验原因",first.reason());
        assertEquals(first,growthService.adjustAsset(id,"EXPERIENCE",5,"EXP-META","经验原因"));
        assertEquals(5L,memberUserGateway.findById(id).experience());
        assertEquals(4,com.lemnt.member.domain.growth.model.ExperienceRecordMetadata.from("EXPERIENCE","SIGN_IN",2).type());
        assertEquals(11,com.lemnt.member.domain.growth.model.ExperienceRecordMetadata.from("EXPERIENCE","TRADE_EXPERIENCE",2).type());
        assertEquals(13,com.lemnt.member.domain.growth.model.ExperienceRecordMetadata.from("EXPERIENCE","TRADE_EXPERIENCE_RETURN",-2).type());
        assertNull(com.lemnt.member.domain.growth.model.ExperienceRecordMetadata.from("EXPERIENCE","UNKNOWN",2).type());
    }
    @org.junit.jupiter.api.Test
    void legacyExperienceCopyKeepsOriginalIdAndUnknownMetadataAndReplay() throws Exception {
        createLegacyAssetTable();
        sql("INSERT INTO member_asset_record(id,member_id,asset_type,delta,balance,biz_type,biz_no,reason,create_time) VALUES(900,"+id+",'EXPERIENCE',7,7,'ADMIN','OLD-EXP','旧经验',CURRENT_TIMESTAMP)");
        sql("UPDATE member_user SET experience=7 WHERE id="+id);
        ScriptUtils.executeSqlScript(schema,new ClassPathResource("member-sql/upgrade/experience_record_split.sql"));
        var copied=experienceRecords.request(id,"ADMIN","OLD-EXP");
        assertEquals(900L,copied.id());assertNull(copied.experienceBizType());assertNull(copied.title());
        assertNull(pointRecords.detail(900L));
        assertEquals(copied,growthService.adjustAsset(id,"EXPERIENCE",7,"OLD-EXP","旧经验"));
        assertEquals(7L,memberUserGateway.findById(id).experience());assertEquals(1,count("member_experience_record"));
        assertEquals(1,count("member_asset_record"));
    }

    @org.junit.jupiter.api.Test
    void typedLedgersRejectMisroutedWritesAndQueriesRejectUnknownType() {
        var now=memberUserGateway.now();
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,()->pointRecords.save(
                new com.lemnt.member.domain.growth.model.AssetRecord(null,id,"EXPERIENCE",1L,1L,"ADMIN","WRONG","错误",now)));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,()->experienceRecords.save(
                new com.lemnt.member.domain.growth.model.AssetRecord(null,id,"POINT",1L,1L,"ADMIN","WRONG","错误",now)));
        assertEquals(0,pointRecords.count(new com.lemnt.member.domain.growth.model.AssetRecordPageFilter(id,"POINT",0,1)));
        assertEquals(0,experienceRecords.count(new com.lemnt.member.domain.growth.model.AssetRecordPageFilter(id,"EXPERIENCE",0,1)));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,()->memberAssetRecordDetailQueryExe.execute(new AssetRecordDetailQry("UNKNOWN",1L)));
    }

    @org.junit.jupiter.api.Test
    void ledgerAuditUsesExistingContextAndStableDatabaseEventTimeOnReplay() {
        com.lemnt.common.security.context.SecurityContextHolder.setUserName("audit-operator");
        try {
            var point=growthService.adjustAsset(id,"POINT",3,"AUDIT-P","原因");
            var experience=growthService.adjustAsset(id,"EXPERIENCE",4,"AUDIT-E","原因");
            var pointRow=template.getMapper(com.lemnt.member.infrastructure.growth.mapper.MemberPointRecordMapper.class).selectById(point.id());
            var expRow=template.getMapper(com.lemnt.member.infrastructure.growth.mapper.MemberExperienceRecordMapper.class).selectById(experience.id());
            assertEquals("audit-operator",pointRow.getCreateBy());assertEquals("audit-operator",pointRow.getUpdateBy());
            assertEquals(pointRow.getCreateTime(),pointRow.getUpdateTime());
            assertEquals("audit-operator",expRow.getCreateBy());assertEquals(expRow.getCreateTime(),expRow.getUpdateTime());
            com.lemnt.common.security.context.SecurityContextHolder.setUserName("retry-operator");
            growthService.adjustAsset(id,"POINT",3,"AUDIT-P","原因");
            var replay=template.getMapper(com.lemnt.member.infrastructure.growth.mapper.MemberPointRecordMapper.class).selectById(point.id());
            assertEquals(pointRow.getUpdateTime(),replay.getUpdateTime());assertEquals("audit-operator",replay.getUpdateBy());
        } finally { com.lemnt.common.security.context.SecurityContextHolder.remove(); }
    }

    @org.junit.jupiter.api.Test
    void levelConfigurationSnapshotsSurviveLaterChangesAndManualReplay() throws Exception {
        var level=growthService.saveLevel(new Level(null,"Gold",1,100L,90,null,null,1));
        growthService.assignLevel(id,level,"SNAPSHOT-LEVEL","原原因");
        var first=memberLevelRecordGateway.levelRecords(id,0,20).get(0);
        assertEquals(1,first.level());assertEquals(90,first.discountPercent());assertEquals(100L,first.recordExperience());assertEquals(100L,first.experience());
        assertEquals("管理员调整为：Gold",first.description());
        sql("UPDATE member_level SET name='Changed',level=2,experience=200,discount_percent=80 WHERE id="+level);
        growthService.assignLevel(id,level,"SNAPSHOT-LEVEL","原原因");
        assertEquals(1,memberLevelRecordGateway.levelRecords(id,0,20).size());
        assertEquals(first,memberLevelRecordGateway.levelRecords(id,0,20).get(0));
        growthService.assignLevel(id,null,"CANCEL-SNAPSHOT","取消原因");
        var cancelled=memberLevelRecordGateway.levelRecords(id,0,20).get(0);
        assertEquals(-100L,cancelled.recordExperience());assertNull(cancelled.discountPercent());assertEquals("管理员取消了等级",cancelled.description());assertEquals(0L,cancelled.experience());
    }
    @org.junit.jupiter.api.Test
    void legacyLevelUpgradeKeepsMemberExperienceAndDoesNotInferCurrentConfiguration() throws Exception {
        var level=growthService.saveLevel(new Level(null,"Current",1,100L,90,null,null,1));
        sql("DROP TABLE member_level_record");
        sql("CREATE TABLE member_level_record(id BIGINT PRIMARY KEY,member_id BIGINT,level_id BIGINT,experience BIGINT NOT NULL,reason VARCHAR(500),create_time DATETIME)");
        sql("INSERT INTO member_level_record VALUES(900,"+id+","+level+",7,'旧原因',CURRENT_TIMESTAMP)");
        ScriptUtils.executeSqlScript(schema,new ClassPathResource("member-sql/upgrade/level_record_snapshot.sql"));
        var old=memberLevelRecordGateway.levelsDetail(900L);
        assertEquals(7L,old.experience());assertEquals("旧原因",old.reason());assertNull(old.recordExperience());assertNull(old.level());assertNull(old.discountPercent());
    }

    @org.junit.jupiter.api.Test
    void automaticLevelRecordStoresThresholdSeparatelyFromMemberExperience() {
        growthService.saveLevel(new Level(null,"Gold",1,100L,90,null,null,1));
        growthService.adjustAsset(id,"EXPERIENCE",150,"AUTO-LEVEL-SNAPSHOT","经验奖励");
        var record=memberLevelRecordGateway.levelRecords(id,0,20).get(0);
        assertEquals(150L,record.experience());assertEquals(100L,record.recordExperience());
        assertEquals(1,record.level());assertEquals(90,record.discountPercent());assertNull(record.description());
    }

    /** 低于最低门槛只更新经验，不按当前空匹配取消等级。 */
    @Test
    void experienceBelowLowestThresholdKeepsExistingLevel() {
        var level = growthService.saveLevel(new Level(null, "Gold", 1, 100L, 90, null, null, 1));
        growthService.assignLevel(id, level, "KEEP-LEVEL", "分配");
        growthService.adjustAsset(id, "EXPERIENCE", -99, "BELOW-LEVEL", "扣减");
        assertEquals(1L, memberUserGateway.findById(id).experience());
        assertEquals(level, memberUserGateway.findById(id).levelId());
        assertEquals(1, memberLevelRecordGateway.levelRecordCount(id));
    }

    /** 所有配置停用或不存在时保留原等级，后续恢复仍可正常计算。 */
    @Test
    void missingEnabledLevelConfigurationKeepsExistingLevel() throws Exception {
        var level = growthService.saveLevel(new Level(null, "Gold", 1, 100L, 90, null, null, 1));
        growthService.assignLevel(id, level, "DISABLED-LEVEL", "分配");
        sql("UPDATE member_level SET status=0 WHERE id=" + level);
        growthService.adjustAsset(id, "EXPERIENCE", 10, "DISABLED-EXPERIENCE", "奖励");
        assertEquals(level, memberUserGateway.findById(id).levelId());
        assertEquals(1, memberLevelRecordGateway.levelRecordCount(id));
        assertEquals(110L, memberUserGateway.findById(id).experience());
    }

    /** 匹配低等级时正常降级，同等级内经验变化不增加等级流水。 */
    @Test
    void experienceDowngradesOnlyToMatchingEnabledLevel() {
        var bronze = growthService.saveLevel(new Level(null, "Bronze", 1, 10L, 95, null, null, 1));
        var gold = growthService.saveLevel(new Level(null, "Gold", 2, 100L, 90, null, null, 1));
        growthService.assignLevel(id, gold, "DOWNGRADE-LEVEL", "分配");
        growthService.adjustAsset(id, "EXPERIENCE", -50, "DOWNGRADE-EXPERIENCE", "扣减");
        assertEquals(bronze, memberUserGateway.findById(id).levelId());
        var record = memberLevelRecordGateway.levelRecords(id, 0, 20).get(0);
        assertEquals(50L, record.experience());
        assertEquals(10L, record.recordExperience());
        assertEquals(1, record.level());
        growthService.adjustAsset(id, "EXPERIENCE", 1, "SAME-LEVEL", "奖励");
        assertEquals(2, memberLevelRecordGateway.levelRecordCount(id));
    }

    /** 经验可超额扣至零，重试按原请求值比对，积分不沿用经验规则。 */
    @Test
    void experienceDeductionFloorsBalanceAndReplaysOriginalDelta() {
        growthService.adjustAsset(id, "EXPERIENCE", 5, "FLOOR-ADD", "奖励");
        var record = growthService.adjustAsset(id, "EXPERIENCE", -10, "FLOOR-SUBTRACT", "扣减");
        assertEquals(-10L, record.delta());
        assertEquals(0L, record.balance());
        assertEquals(0L, memberUserGateway.findById(id).experience());
        assertEquals(record, growthService.adjustAsset(id, "EXPERIENCE", -10, "FLOOR-SUBTRACT", "扣减"));
        assertBusinessException(409, () -> growthService.adjustAsset(id, "EXPERIENCE", -9, "FLOOR-SUBTRACT", "扣减"));
        assertBusinessException(400, () -> growthService.adjustAsset(id, "POINT", -1, "FLOOR-POINT", "扣减"));
    }
    /** 源筛选使用实际快照，精确匹配与分页总数共用条件，历史NULL只在未筛选时可见。 */
    @Test
    void experienceSourceFiltersShareCountAndKeepTypeZeroAndUnknownDistinct() throws Exception {
        growthService.adjustAsset(id, "EXPERIENCE", 5, "FILTER-E1", "原因1");
        growthService.adjustAsset(id, "EXPERIENCE", 6, "FILTER-E2", "原因2");
        growthService.adjustAsset(other, "EXPERIENCE", 7, "FILTER-OTHER", "其他会员");
        experienceRecords.save(new com.lemnt.member.domain.growth.model.AssetRecord(
                null, id, "EXPERIENCE", 1L, 12L, "ADMIN", "FILTER-OLD", "旧记录", memberUserGateway.now()));
        sql("UPDATE member_experience_record SET create_time='2026-10-30 10:00:00' WHERE biz_id='FILTER-E1'");
        sql("UPDATE member_experience_record SET create_time='2026-10-30 11:00:00' WHERE biz_id='FILTER-E2'");
        var start = LocalDateTime.of(2026, 10, 30, 10, 0);
        var end = start.plusHours(1);
        var query = new MemberExperienceRecordAllPageQueryExe(experienceRecords);
        var first = query.execute(new ExperienceRecordAllPageQry(id, 1, 1, null, 0, "管理员调整", start, end));
        var second = query.execute(new ExperienceRecordAllPageQry(id, 2, 1, null, 0, "管理员调整", start, end));
        assertEquals(2, first.getTotal());
        assertEquals(2, second.getTotal());
        assertEquals("FILTER-E2", first.getRecords().get(0).bizNo());
        assertEquals("FILTER-E1", second.getRecords().get(0).bizNo());
        assertEquals(0, first.getRecords().get(0).experienceBizType());
        assertEquals(1, query.execute(new ExperienceRecordAllPageQry(id, 1, 20, " FILTER-E1 ", 0, " 管理员调整 ", start, end)).getTotal());
        assertEquals(0, query.execute(new ExperienceRecordAllPageQry(id, 1, 20, "FILTER-E", 0, null, null, null)).getTotal());
        assertEquals(0, query.execute(new ExperienceRecordAllPageQry(id, 1, 20, null, 0, "管理员", null, null)).getTotal());
        assertEquals(3, query.execute(new ExperienceRecordAllPageQry(id, 1, 20, null, null, null, null, null)).getTotal());
        assertEquals(0, query.execute(new ExperienceRecordAllPageQry(id, 1, 20, "FILTER-OLD", 0, null, null, null)).getTotal());
        assertEquals(1, query.execute(new ExperienceRecordAllPageQry(id, 1, 20, null, 0, null, end, null)).getTotal());
        assertEquals(1, query.execute(new ExperienceRecordAllPageQry(id, 1, 20, null, 0, null, null, start)).getTotal());
        assertEquals(0, query.execute(new ExperienceRecordAllPageQry(id, 1, 20, null, 4, null, null, null)).getTotal());
    }

    /** 时间和类型在应用入口统一校验，允许查询尚无写入路径的源枚举。 */
    @Test
    void experienceFiltersRejectInvalidRangesAndAcceptAllSourceTypes() {
        var query = new MemberExperienceRecordAllPageQueryExe(experienceRecords);
        var now = LocalDateTime.now();
        assertBusinessException(400, () -> query.execute(new ExperienceRecordAllPageQry(id, 1, 20, null, 2, null, null, null)));
        assertBusinessException(400, () -> query.execute(new ExperienceRecordAllPageQry(id, 1, 20, null, null, null, now, now.minusSeconds(1))));
        for (int type : new int[] {0, 1, 4, 5, 11, 12, 13}) {
            assertEquals(0, query.execute(new ExperienceRecordAllPageQry(id, 1, 20, null, type, null, null, null)).getTotal());
        }
    }
    /** 等级筛选读取记录编号，取消记录和其他会员不混入，时间两端均包含。 */
    @Test
    void levelRecordFiltersShareCountAndPreserveCancellationHistory() throws Exception {
        var bronze = growthService.saveLevel(new Level(null, "Bronze", 1, 10L, 95, null, null, 1));
        var gold = growthService.saveLevel(new Level(null, "Gold", 2, 100L, 90, null, null, 1));
        growthService.assignLevel(id, gold, "LEVEL-FILTER-A", "第一次");
        growthService.assignLevel(id, bronze, "LEVEL-FILTER-B", "降级");
        growthService.assignLevel(id, gold, "LEVEL-FILTER-C", "第二次");
        growthService.assignLevel(id, null, "LEVEL-FILTER-CANCEL", "取消");
        growthService.assignLevel(other, gold, "LEVEL-FILTER-OTHER", "其他会员");
        sql("UPDATE member_level_record SET create_time='2026-10-30 10:30:00'");
        var goldRows = memberLevelRecordGateway.levelRecords(id, 0, 20).stream()
                .filter(v -> gold.equals(v.levelId())).toList();
        sql("UPDATE member_level_record SET create_time='2026-10-30 10:00:00' WHERE id=" + goldRows.get(1).id());
        sql("UPDATE member_level_record SET create_time='2026-10-30 11:00:00' WHERE id=" + goldRows.get(0).id());
        var start = LocalDateTime.of(2026, 10, 30, 10, 0);
        var end = start.plusHours(1);
        var first = memberLevelRecordAllPageQueryExe.execute(new LevelRecordAllPageQry(id, 1, 1, gold, start, end));
        var second = memberLevelRecordAllPageQueryExe.execute(new LevelRecordAllPageQry(id, 2, 1, gold, start, end));
        assertEquals(2, first.getTotal());
        assertEquals(2, second.getTotal());
        assertEquals(goldRows.get(0).id(), first.getRecords().get(0).id());
        assertEquals(goldRows.get(1).id(), second.getRecords().get(0).id());
        assertEquals(4, memberLevelRecordAllPageQueryExe.execute(new LevelRecordAllPageQry(id, 1, 20)).getTotal());
        assertEquals(3, memberLevelRecordAllPageQueryExe.execute(new LevelRecordAllPageQry(null, 1, 20, gold, start, end)).getTotal());
        assertEquals(1, memberLevelRecordAllPageQueryExe.execute(new LevelRecordAllPageQry(id, 1, 20, gold, end, null)).getTotal());
        assertEquals(1, memberLevelRecordAllPageQueryExe.execute(new LevelRecordAllPageQry(id, 1, 20, gold, null, start)).getTotal());
        assertNull(memberLevelRecordGateway.levelRecords(id, 0, 1).get(0).levelId());
        assertEquals(0, memberLevelRecordAllPageQueryExe.execute(new LevelRecordAllPageQry(id, 1, 20, Long.MAX_VALUE, null, null)).getTotal());
        assertNotNull(memberLevelRecordGateway.levelsDetail(goldRows.get(0).id()));
    }

    /** 校验发生在应用入口，内部调用同样不能绕过编号和时间约束。 */
    @Test
    void levelRecordFiltersRejectInvalidIdAndReversedTime() {
        var now = LocalDateTime.now();
        assertBusinessException(400, () -> memberLevelRecordAllPageQueryExe.execute(new LevelRecordAllPageQry(id, 1, 20, 0L, null, null)));
        assertBusinessException(400, () -> memberLevelRecordAllPageQueryExe.execute(new LevelRecordAllPageQry(id, 1, 20, null, now, now.minusSeconds(1))));
    }
    /** 积分标题按源包含匹配，不能用原因替代缺失的历史标题。 */
    @Test
    void pointTitleFilterCombinesWithMemberNicknameAndType() {
        growthService.adjustAsset(id, "POINT", 5, "TITLE-P1", "原原因");
        growthService.adjustAsset(id, "POINT", 2, "TITLE-P2", "原原因");
        growthService.adjustAsset(other, "POINT", 3, "TITLE-OTHER", "原原因");
        pointRecords.save(new com.lemnt.member.domain.growth.model.AssetRecord(
                null, id, "POINT", 1L, 8L, "ADMIN", "TITLE-OLD", "管理员修改", memberUserGateway.now()));
        var first = memberAssetRecordAllPageQueryExe.execute(new AssetRecordAllPageQry("POINT", id, 1, 1, "one", "ADMIN", 2, " 管理 "));
        var second = memberAssetRecordAllPageQueryExe.execute(new AssetRecordAllPageQry("POINT", id, 2, 1, "one", "ADMIN", 2, "管理"));
        assertEquals(2, first.getTotal());
        assertEquals(2, second.getTotal());
        assertEquals("TITLE-P2", first.getRecords().get(0).bizNo());
        assertEquals("TITLE-P1", second.getRecords().get(0).bizNo());
        assertEquals(0, memberAssetRecordAllPageQueryExe.execute(new AssetRecordAllPageQry("POINT", id, 1, 20, null, null, null, "不存在")).getTotal());
        assertEquals(3, memberAssetRecordAllPageQueryExe.execute(new AssetRecordAllPageQry("POINT", id, 1, 20)).getTotal());
    }

    /** 收入、支出、零流水及日期边界独立判断，始终限制当前会员。 */
    @Test
    void appPointDirectionAndTimeFiltersKeepFalseAndMemberOwnership() throws Exception {
        growthService.adjustAsset(id, "POINT", 10, "DIRECTION-P1", "收入1");
        growthService.adjustAsset(id, "POINT", -3, "DIRECTION-P2", "支出");
        growthService.adjustAsset(id, "POINT", 2, "DIRECTION-P3", "收入2");
        growthService.adjustAsset(other, "POINT", 3, "DIRECTION-OTHER", "其他会员");
        pointRecords.save(new com.lemnt.member.domain.growth.model.AssetRecord(
                null, id, "POINT", 0L, 9L, "TRADE_CLOSED", "DIRECTION-ZERO", "旧零流水", memberUserGateway.now()));
        sql("UPDATE member_point_record SET create_time='2026-10-30 10:30:00'");
        sql("UPDATE member_point_record SET create_time='2026-10-30 10:00:00' WHERE biz_id='DIRECTION-P1'");
        sql("UPDATE member_point_record SET create_time='2026-10-30 11:00:00' WHERE biz_id='DIRECTION-P3'");
        var start = LocalDateTime.of(2026, 10, 30, 10, 0);
        var end = start.plusHours(1);
        var query = new MemberAssetRecordPageQueryExe(pointRecords, experienceRecords);
        var income = query.execute(new AssetRecordPageQry(id, "POINT", 1, 1, true, start, end));
        var incomeNext = query.execute(new AssetRecordPageQry(id, "POINT", 2, 1, true, start, end));
        var expense = query.execute(new AssetRecordPageQry(id, "POINT", 1, 20, false, start, end));
        assertEquals(2, income.getTotal());
        assertEquals(2, incomeNext.getTotal());
        assertEquals("DIRECTION-P3", income.getRecords().get(0).bizNo());
        assertEquals("DIRECTION-P1", incomeNext.getRecords().get(0).bizNo());
        assertEquals(1, expense.getTotal());
        assertEquals(-3L, expense.getRecords().get(0).delta());
        assertEquals(4, query.execute(new AssetRecordPageQry(id, "POINT", 1, 20, null, start, end)).getTotal());
        assertEquals(1, query.execute(new AssetRecordPageQry(id, "POINT", 1, 20, true, end, null)).getTotal());
        assertEquals(1, query.execute(new AssetRecordPageQry(id, "POINT", 1, 20, true, null, start)).getTotal());
        assertBusinessException(400, () -> query.execute(new AssetRecordPageQry(id, "POINT", 1, 20, false, end, start)));
        assertBusinessException(400, () -> query.execute(new AssetRecordPageQry(id, "EXPERIENCE", 1, 20, false, null, null)));
    }
    /** 同一次数据库事件同时确定签到日期和审计时间，拒绝重复不改变时间。 */
    @Test
    void signEventTimeAndAuditRemainStableAfterDuplicateRejection() {
        growthService.createSignConfig(new SignConfig(1, 10L, 2L, 1));
        var before = memberUserGateway.now().withNano(0);
        var first = growthService.sign(id);
        var after = memberUserGateway.now().withNano(0);
        assertNotNull(first.createTime());
        assertFalse(first.createTime().isBefore(before));
        assertFalse(first.createTime().isAfter(after));
        assertEquals(first.signDate(), first.createTime().toLocalDate());
        assertBusinessException(409, () -> growthService.sign(id));
        var row = template.getMapper(MemberSignRecordMapper.class).selectById(first.id());
        assertEquals(first.createTime(), row.getCreateTime());
        assertEquals(row.getCreateTime(), row.getUpdateTime());
    }

    /** 真实时间和连续天数共同过滤，旧NULL时间不按业务日期补造。 */
    @Test
    void signDayAndTimeFiltersShareCountAndExcludeUnknownTimes() throws Exception {
        var start = LocalDateTime.of(2026, 10, 29, 10, 0);
        var end = LocalDateTime.of(2026, 10, 30, 11, 0);
        memberSignRecordGateway.signSave(new SignRecord(null, id, start.toLocalDate(), 1, 1L, 0L, null, start));
        memberSignRecordGateway.signSave(new SignRecord(null, id, end.toLocalDate(), 1, 2L, 0L, null, end));
        memberSignRecordGateway.signSave(new SignRecord(null, id, start.minusDays(1).toLocalDate(), 2, 3L, 0L, null, start.minusDays(1)));
        memberSignRecordGateway.signSave(new SignRecord(null, other, end.toLocalDate(), 1, 4L, 0L, null, end));
        sql("INSERT INTO member_sign_in_record(user_id,sign_date,`day`,point,experience) VALUES(" + id + ",'2026-10-27',1,1,0)");
        var first = memberSignRecordAllPageQueryExe.execute(new SignRecordAllPageQry(id, 1, 1, "one", 1, start, end));
        var second = memberSignRecordAllPageQueryExe.execute(new SignRecordAllPageQry(id, 2, 1, "one", 1, start, end));
        assertEquals(2, first.getTotal());
        assertEquals(2, second.getTotal());
        assertEquals(end, first.getRecords().get(0).createTime());
        assertEquals(start, second.getRecords().get(0).createTime());
        assertEquals(3, memberSignRecordAllPageQueryExe.execute(new SignRecordAllPageQry(id, 1, 20, null, 1, null, null)).getTotal());
        assertEquals(1, memberSignRecordAllPageQueryExe.execute(new SignRecordAllPageQry(id, 1, 20, null, 1, end, null)).getTotal());
        assertEquals(1, memberSignRecordAllPageQueryExe.execute(new SignRecordAllPageQry(id, 1, 20, null, 1, null, start)).getTotal());
        assertEquals(0, memberSignRecordAllPageQueryExe.execute(new SignRecordAllPageQry(id, 1, 20, "two", 1, start, end)).getTotal());
        assertBusinessException(400, () -> memberSignRecordAllPageQueryExe.execute(new SignRecordAllPageQry(id, 1, 20, null, 0, null, null)));
        assertBusinessException(400, () -> memberSignRecordAllPageQueryExe.execute(new SignRecordAllPageQry(id, 1, 20, null, 1, end, start)));
    }

    /** 旧表升级保存业务日期和奖励，未知真实时间/操作者保持NULL。 */
    @Test
    void legacySignUpgradeRenamesSourceFieldsWithoutInventingTime() throws Exception {
        sql("DROP TABLE member_sign_in_record");
        sql("CREATE TABLE member_sign_record(id BIGINT PRIMARY KEY,member_id BIGINT NOT NULL,sign_date DATE NOT NULL,signin_day INT NOT NULL,point BIGINT NOT NULL,experience BIGINT NOT NULL,UNIQUE(member_id,sign_date))");
        sql("INSERT INTO member_sign_record VALUES(900," + id + ",'2026-10-29',3,5,2)");
        ScriptUtils.executeSqlScript(schema, new ClassPathResource("member-sql/upgrade/sign_record_time.sql"));
        var old = memberSignRecordGateway.signDate(id, java.time.LocalDate.of(2026, 10, 29));
        assertEquals(900L, old.id());
        assertEquals(3, old.day());
        assertEquals(5L, old.point());
        assertEquals(2L, old.experience());
        assertNull(old.createTime());
        var row = template.getMapper(MemberSignRecordMapper.class).selectById(900L);
        assertNull(row.getCreateBy());
        assertNull(row.getUpdateBy());
        assertNull(row.getUpdateTime());
        assertEquals(1, memberSignRecordAllPageQueryExe.execute(new SignRecordAllPageQry(id, 1, 20)).getTotal());
    }
    /** 最大启用天数决定新一轮，停用的高天数不延长周期；断签重置到第一天。 */
    @Test
    void signCycleWrapAndInterruptedDaysFollowEnabledConfiguration() {
        growthService.createSignConfig(new SignConfig(1, 5L, 0L, 1));
        growthService.createSignConfig(new SignConfig(2, 10L, 0L, 1));
        growthService.createSignConfig(new SignConfig(3, 99L, 0L, 0));
        var now = memberUserGateway.now().withNano(0);
        memberSignRecordGateway.signSave(new SignRecord(null, id, now.minusDays(1).toLocalDate(), 2, 10L, 0L, null, now.minusDays(1)));
        memberSignRecordGateway.signSave(new SignRecord(null, other, now.minusDays(2).toLocalDate(), 2, 10L, 0L, null, now.minusDays(2)));
        var wrapped = growthService.sign(id);
        var interrupted = growthService.sign(other);
        assertEquals(1, wrapped.day());
        assertEquals(1, interrupted.day());
        assertEquals(5L, wrapped.point());
        assertEquals(5L, interrupted.point());
        assertEquals(5L, memberUserGateway.findById(id).points());
        assertEquals(5L, memberUserGateway.findById(other).points());
    }

    /** 连续天数缺少当天启用规则时仍记签到但不发奖励；无启用配置则明确拒绝。 */
    @Test
    void disabledCurrentDayDoesNotInventRewardsAndEmptyConfigurationRejects() throws Exception {
        growthService.createSignConfig(new SignConfig(1, 5L, 0L, 1));
        growthService.createSignConfig(new SignConfig(2, 99L, 99L, 0));
        growthService.createSignConfig(new SignConfig(3, 10L, 0L, 1));
        var yesterday = memberUserGateway.now().withNano(0).minusDays(1);
        memberSignRecordGateway.signSave(new SignRecord(null, id, yesterday.toLocalDate(), 1, 5L, 0L, null, yesterday));
        var signed = growthService.sign(id);
        assertEquals(2, signed.day());
        assertEquals(0L, signed.point());
        assertEquals(0L, signed.experience());
        assertNull(pointRecords.request(id, "SIGN_IN", signed.id().toString()));
        assertNull(experienceRecords.request(id, "SIGN_IN", signed.id().toString()));
        assertEquals(0L, memberUserGateway.findById(id).points());
        sql("UPDATE member_sign_in_config SET status=0");
        assertBusinessException(409, () -> growthService.sign(other));
        assertEquals(0, memberSignRecordGateway.signCount(other));
    }

    /** 连续汇总只认今天和昨天，未来记录不能被当作连续签到。 */
    @Test
    void signSummaryMatchesTodayYesterdayAndRejectsFutureContinuity() throws Exception {
        var now = memberUserGateway.now().withNano(0);
        var recordId = memberSignRecordGateway.signSave(new SignRecord(null, id, now.plusDays(1).toLocalDate(), 3, 1L, 0L, null, now.plusDays(1)));
        var summary = new com.lemnt.member.application.growth.executor.MemberSignSummaryQueryExe(memberSignRecordGateway, memberUserGateway);
        for (int offset : new int[] {1, 0, -1, -2}) {
            sql("UPDATE member_sign_in_record SET sign_date='" + now.plusDays(offset).toLocalDate() + "' WHERE id=" + recordId);
            var result = summary.execute(new com.lemnt.member.application.growth.qry.SignSummaryQry(id));
            assertEquals(1, result.totalDay());
            assertEquals(offset == 0 || offset == -1 ? 3 : 0, result.continuousDay());
            assertEquals(offset == 0, result.todaySignIn());
        }
    }
    /** 新增不覆盖已有天数，修改和删除始终按记录ID。 */
    @Test
    void signConfigurationCrudUsesIdAndRejectsDuplicateDay() {
        var first = growthService.createSignConfig(new SignConfig(1, 5L, 2L, 1));
        var second = growthService.createSignConfig(new SignConfig(2, 10L, 0L, 1));
        assertNotEquals(first, second);
        assertBusinessException(409, () -> growthService.createSignConfig(new SignConfig(1, 99L, 99L, 1)));
        assertEquals(5L, memberSignConfigGateway.signConfig(first).point());
        growthService.updateSignConfig(new SignConfig(first, 3, 7L, 1L, 0));
        assertNull(memberSignConfigGateway.signConfigByDay(1));
        assertEquals(3, memberSignConfigGateway.signConfig(first).day());
        assertBusinessException(409, () -> growthService.updateSignConfig(new SignConfig(first, 2, 99L, 0L, 1)));
        assertEquals(7L, memberSignConfigGateway.signConfig(first).point());
        assertBusinessException(404, () -> growthService.updateSignConfig(new SignConfig(Long.MAX_VALUE, 4, 1L, 0L, 1)));
        assertTrue(growthService.deleteSignConfig(first));
        assertNull(memberSignConfigGateway.signConfig(first));
        assertBusinessException(404, () -> growthService.deleteSignConfig(first));
        assertEquals(second, memberSignConfigGateway.signConfigs().get(0).id());
        assertEquals(1, memberSignConfigGateway.signConfigs().size());
    }

    /** 直接并发插入验证数据库唯一约束及统一冲突，不能悄悄覆盖。 */
    @Test
    void concurrentSignConfigurationCreationHasOneWinner() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Callable<Object> create = () -> {
                try {
                    return memberSignConfigGateway.create(new SignConfig(1, 5L, 0L, 1));
                } catch (ServiceException exception) {
                    return exception;
                }
            };
            var a = pool.submit(create);
            var b = pool.submit(create);
            var results = List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
            assertEquals(1, results.stream().filter(Long.class::isInstance).count());
            var failures = results.stream().filter(ServiceException.class::isInstance).map(ServiceException.class::cast).toList();
            assertEquals(1, failures.size());
            assertEquals(409, failures.get(0).getCode());
            assertEquals(1, memberSignConfigGateway.signConfigs().size());
        } finally {
            pool.shutdownNow();
        }
    }

    /** 真实旧结构升级保留业务配置，生成的新ID仅作定位，不推算历史审计。 */
    @Test
    void legacySignConfigUpgradeAddsIdsAndKeepsUnknownAudit() throws Exception {
        sql("DROP TABLE member_sign_in_config");
        sql("CREATE TABLE member_sign_config(signin_day INT PRIMARY KEY,point BIGINT NOT NULL,experience BIGINT NOT NULL,status INT NOT NULL)");
        sql("INSERT INTO member_sign_config VALUES(7,10,2,0),(3,5,0,1)");
        ScriptUtils.executeSqlScript(schema, new ClassPathResource("member-sql/upgrade/sign_config_id.sql"));
        var old = memberSignConfigGateway.signConfigByDay(7);
        assertNotNull(old.id());
        assertEquals(10L, old.point());
        assertEquals(2L, old.experience());
        assertEquals(0, old.status());
        var row = template.getMapper(MemberSignConfigMapper.class).selectById(old.id());
        assertNull(row.getCreateTime());
        assertNull(row.getCreateBy());
        assertNull(row.getUpdateTime());
        assertNull(row.getUpdateBy());
        var created = growthService.createSignConfig(new SignConfig(1, 1L, 0L, 1));
        assertNotEquals(old.id(), created);
        assertEquals(3, memberSignConfigGateway.signConfigs().size());
    }
    /** 空配置先能管理等级，再首次保存积分配置；更新按真实ID而不是固定1。 */
    @Test
    void missingPointConfigurationCanBeCreatedAndReadByTradeClient() throws Exception {
        sql("DELETE FROM member_config");
        assertNull(memberConfigGateway.config());
        var client = new com.lemnt.member.application.dubbo.MemberGrowthClientImpl(growthService, memberConfigGateway);
        assertBusinessException(409, () -> client.getPointConfig());
        assertNotNull(growthService.saveLevel(new Level(null, "NoConfig", 1, 10L, 95, null, null, 1)));
        var value = new com.lemnt.member.domain.growth.model.Config(true, 5, 2, 3);
        assertTrue(growthService.saveConfig(value));
        assertEquals(value, memberConfigGateway.config());
        var external = client.getPointConfig();
        assertEquals(5, external.unitPrice());
        assertEquals(2, external.maxPoints());
        assertEquals(3, external.givePerYuan());
        assertTrue(growthService.saveConfig(new com.lemnt.member.domain.growth.model.Config(false, 0, 0, 0)));
        assertEquals(1, count("member_config"));
        assertFalse(memberConfigGateway.config().pointTradeDeductEnable());
        assertBusinessException(400, () -> growthService.saveConfig(new com.lemnt.member.domain.growth.model.Config(true, 0, 2, 1)));
        assertFalse(memberConfigGateway.config().pointTradeDeductEnable());
    }

    /** 并发首次保存由基础事务锁串行化，不生成两份配置。 */
    @Test
    void concurrentInitialPointConfigurationSaveCreatesOnlyOneRow() throws Exception {
        sql("DELETE FROM member_config");
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> growthService.saveConfig(new com.lemnt.member.domain.growth.model.Config(true, 5, 2, 1)));
            var b = pool.submit(() -> growthService.saveConfig(new com.lemnt.member.domain.growth.model.Config(true, 3, 4, 2)));
            assertTrue(a.get(15, TimeUnit.SECONDS));
            assertTrue(b.get(15, TimeUnit.SECONDS));
            assertEquals(1, count("member_config"));
        } finally {
            pool.shutdownNow();
        }
    }

    /** 旧配置ID与金额参数保持，新增审计未知NULL，后续修改只更新实际首条。 */
    @Test
    void legacyPointConfigurationUpgradePreservesRealIdAndAudit() throws Exception {
        sql("DROP TABLE member_config");
        sql("CREATE TABLE member_config(id INT PRIMARY KEY,point_trade_deduct_enable BOOLEAN NOT NULL,point_trade_deduct_unit_price INT NOT NULL,point_trade_deduct_max_price INT NOT NULL,point_trade_give_point INT NOT NULL)");
        sql("INSERT INTO member_config VALUES(77,TRUE,5,2,3)");
        ScriptUtils.executeSqlScript(schema, new ClassPathResource("member-sql/upgrade/point_config_audit.sql"));
        var mapper = template.getMapper(MemberConfigMapper.class);
        var original = mapper.selectById(77L);
        assertNull(original.getCreateTime());
        assertNull(original.getCreateBy());
        assertNull(original.getUpdateTime());
        assertEquals(5, memberConfigGateway.config().pointTradeDeductUnitPrice());
        growthService.saveConfig(new com.lemnt.member.domain.growth.model.Config(true, 3, 4, 2));
        assertEquals(3, mapper.selectById(77L).getPointTradeDeductUnitPrice());
        assertNull(mapper.selectById(1L));
        assertNull(mapper.selectById(77L).getCreateTime());
        assertNotNull(mapper.selectById(77L).getUpdateTime());
    }
    /** 首次配置写入失败必须回滚，去掉故障约束后可以正常重新保存。 */
    @Test
    void failedInitialPointConfigurationWriteRollsBackAndCanRetry() throws Exception {
        sql("DELETE FROM member_config");
        sql("ALTER TABLE member_config ADD CONSTRAINT reject_config CHECK(point_trade_give_point<0)");
        var value = new com.lemnt.member.domain.growth.model.Config(true, 5, 2, 3);
        assertThrows(Exception.class, () -> growthService.saveConfig(value));
        assertEquals(0, count("member_config"));
        sql("ALTER TABLE member_config DROP CONSTRAINT reject_config");
        assertTrue(growthService.saveConfig(value));
        assertEquals(value, memberConfigGateway.config());
        assertEquals(1, count("member_config"));
    }
    /** 等级名称/状态筛选与启用列表按源条件，清空图片仍通过BaseMapper保存。 */
    @Test
    void levelManagementFiltersAndNullableImagesUseRealRows() {
        var bronze = growthService.saveLevel(new Level(null, "Bronze", 1, 10L, 95, "old-icon", "old-background", 1));
        var gold = growthService.saveLevel(new Level(null, "Gold", 2, 100L, 90, null, null, 0));
        var query = new com.lemnt.member.application.growth.executor.MemberLevelListQueryExe(memberLevelGateway);
        var filtered = query.execute(new com.lemnt.member.application.growth.qry.LevelListQry(false, " Go ", 0));
        assertEquals(1, filtered.size());
        assertEquals(gold, filtered.get(0).id());
        assertEquals(1, query.execute(new com.lemnt.member.application.growth.qry.LevelListQry(true)).size());
        assertEquals(0, query.execute(new com.lemnt.member.application.growth.qry.LevelListQry(true, "Gold", null)).size());
        assertBusinessException(400, () -> query.execute(new com.lemnt.member.application.growth.qry.LevelListQry(false, null, 2)));
        var mapper = template.getMapper(MemberLevelMapper.class);
        var created = mapper.selectById(bronze).getCreateTime();
        assertNotNull(created);
        growthService.saveLevel(new Level(bronze, "Bronze", 1, 10L, 95, null, null, 1));
        var saved = mapper.selectById(bronze);
        assertNull(saved.getIcon());
        assertNull(saved.getBackgroundUrl());
        assertEquals(created, saved.getCreateTime());
        assertNotNull(saved.getUpdateTime());
    }

    /** 使用中等级明确拒绝，解除分配后可删除且历史快照不被删除。 */
    @Test
    void levelDeletionRejectsUsageAndKeepsHistoricalSnapshots() {
        var level = growthService.saveLevel(new Level(null, "Gold", 1, 100L, 90, null, null, 1));
        growthService.assignLevel(id, level, "DELETE-LEVEL-ASSIGN", "分配");
        assertBusinessException(409, () -> growthService.deleteLevel(level));
        assertNotNull(memberLevelGateway.level(level));
        growthService.assignLevel(id, null, "DELETE-LEVEL-CANCEL", "取消");
        assertTrue(growthService.deleteLevel(level));
        assertNull(memberLevelGateway.level(level));
        assertEquals(1, memberLevelRecordGateway.levelRecords(id, 0, 20).stream().filter(v -> level.equals(v.levelId())).count());
        assertBusinessException(404, () -> growthService.deleteLevel(level));
    }

    /** 旧表审计升级只加未知NULL，不推断创建时间。 */
    @Test
    void legacyLevelAuditUpgradePreservesConfigurationAndUnknownHistory() throws Exception {
        // 保留旧结构本就存在的唯一键及会员外键，只移除本批新增审计列。
        sql("ALTER TABLE member_level DROP COLUMN create_time");
        sql("ALTER TABLE member_level DROP COLUMN create_by");
        sql("ALTER TABLE member_level DROP COLUMN update_time");
        sql("ALTER TABLE member_level DROP COLUMN update_by");
        sql("INSERT INTO member_level VALUES(900,'Old',1,100,90,'icon','background',1)");
        ScriptUtils.executeSqlScript(schema, new ClassPathResource("member-sql/upgrade/level_audit_fields.sql"));
        var mapper = template.getMapper(MemberLevelMapper.class);
        var old = mapper.selectById(900L);
        assertNull(old.getCreateTime());
        assertNull(old.getCreateBy());
        assertNull(old.getUpdateTime());
        assertEquals(100L, memberLevelGateway.level(900L).experience());
        growthService.saveLevel(new Level(900L, "Old", 1, 100L, 90, null, null, 1));
        assertNull(mapper.selectById(900L).getCreateTime());
        assertNotNull(mapper.selectById(900L).getUpdateTime());
        assertNull(mapper.selectById(900L).getIcon());
    }
    /** 两种分页各用独立条件，时间端点/状态0与历史NULL均不混淆。 */
    @Test
    void groupAndTagPagesFilterRealCreationTimeWithConsistentCounts() throws Exception {
        var g1 = userService.saveGroup(new Group(null, "G-one", null, 0));
        var g2 = userService.saveGroup(new Group(null, "G-two", null, 0));
        userService.saveGroup(new Group(null, "G-enabled", null, 1));
        var t1 = userService.saveTag(new Tag(null, "T-one", null));
        var t2 = userService.saveTag(new Tag(null, "T-two", null));
        var start = LocalDateTime.of(2026, 10, 30, 10, 0);
        var end = start.plusHours(1);
        sql("UPDATE member_group SET create_time='2026-10-30 10:00:00' WHERE id=" + g1);
        sql("UPDATE member_group SET create_time='2026-10-30 11:00:00' WHERE id=" + g2);
        sql("UPDATE member_tag SET create_time='2026-10-30 10:00:00' WHERE id=" + t1);
        sql("UPDATE member_tag SET create_time='2026-10-30 11:00:00' WHERE id=" + t2);
        sql("INSERT INTO member_group(name,remark,status) VALUES('G-old',NULL,0)");
        sql("INSERT INTO member_tag(name,remark) VALUES('T-old',NULL)");
        var first = memberGroupPageQueryExe.execute(new GroupPageQry("G-", 0, 1, 1, start, end));
        var second = memberGroupPageQueryExe.execute(new GroupPageQry("G-", 0, 2, 1, start, end));
        assertEquals(2, first.getTotal());
        assertEquals(2, second.getTotal());
        assertEquals(g2, first.getRecords().get(0).id());
        assertEquals(g1, second.getRecords().get(0).id());
        assertEquals(end, first.getRecords().get(0).createTime());
        var tags = memberTagPageQueryExe.execute(new TagPageQry("T-", 1, 1, start, end));
        assertEquals(2, tags.getTotal());
        assertEquals(t2, tags.getRecords().get(0).id());
        assertEquals(3, memberGroupPageQueryExe.execute(new GroupPageQry("G-", 0, 1, 20)).getTotal());
        assertEquals(3, memberTagPageQueryExe.execute(new TagPageQry("T-", 1, 20)).getTotal());
        assertEquals(1, memberGroupPageQueryExe.execute(new GroupPageQry("G-", 0, 1, 20, end, null)).getTotal());
        assertEquals(1, memberTagPageQueryExe.execute(new TagPageQry("T-", 1, 20, null, start)).getTotal());
        assertBusinessException(400, () -> memberGroupPageQueryExe.execute(new GroupPageQry(null, null, 1, 20, end, start)));
        assertBusinessException(400, () -> memberTagPageQueryExe.execute(new TagPageQry(null, 1, 20, end, start)));
    }

    /** 基础CRUD保留备注NULL语义，唯一冲突统一业务错误且不覆盖原行。 */
    @Test
    void groupAndTagCrudKeepNullableRemarkAndAuditOnConflict() {
        var group = userService.saveGroup(new Group(null, "UniqueGroup", "备注", 1));
        var tag = userService.saveTag(new Tag(null, "UniqueTag", "备注"));
        var groupCreated = memberGroupGateway.group(group).createTime();
        var tagCreated = memberTagGateway.tag(tag).createTime();
        assertNotNull(groupCreated);
        assertNotNull(tagCreated);
        userService.saveGroup(new Group(group, "UniqueGroup", null, 0));
        userService.saveTag(new Tag(tag, "UniqueTag", null));
        assertNull(memberGroupGateway.group(group).remark());
        assertNull(memberTagGateway.tag(tag).remark());
        assertEquals(groupCreated, memberGroupGateway.group(group).createTime());
        assertEquals(tagCreated, memberTagGateway.tag(tag).createTime());
        assertBusinessException(409, () -> userService.saveGroup(new Group(null, "UniqueGroup", "不得覆盖", 1)));
        assertBusinessException(409, () -> userService.saveTag(new Tag(null, "UniqueTag", "不得覆盖")));
        assertBusinessException(404, () -> userService.deleteGroup(Long.MAX_VALUE));
        assertBusinessException(404, () -> userService.deleteTag(Long.MAX_VALUE));
    }

    /** 升级保留分组/标签现有外键与唯一键，旧创建时间及操作者未知NULL。 */
    @Test
    void legacyGroupTagAuditUpgradePreservesUnknownTimeAndExistingConstraints() throws Exception {
        for (String table : new String[] {"member_group", "member_tag"}) {
            for (String column : new String[] {"create_time", "create_by", "update_time", "update_by"}) {
                sql("ALTER TABLE " + table + " DROP COLUMN " + column);
            }
        }
        sql("INSERT INTO member_group VALUES(900,'OldGroup','old',1)");
        sql("INSERT INTO member_tag VALUES(901,'OldTag','old')");
        ScriptUtils.executeSqlScript(schema, new ClassPathResource("member-sql/upgrade/group_tag_audit_fields.sql"));
        assertNull(memberGroupGateway.group(900L).createTime());
        assertNull(memberTagGateway.tag(901L).createTime());
        userService.saveGroup(new Group(900L, "OldGroup", null, 1));
        userService.saveTag(new Tag(901L, "OldTag", null));
        assertNull(memberGroupGateway.group(900L).createTime());
        assertNull(memberTagGateway.tag(901L).createTime());
        assertNotNull(template.getMapper(MemberGroupMapper.class).selectById(900L).getUpdateTime());
        assertNotNull(template.getMapper(MemberTagMapper.class).selectById(901L).getUpdateTime());
        assertNull(template.getMapper(MemberGroupMapper.class).selectById(900L).getCreateBy());
    }
    /** 源列表按标签任一命中，命中多个标签不重复会员，组合条件与总数一致。 */
    @Test
    void memberUserFiltersMatchAnyTagAndKeepCombinedPageCount() throws Exception {
        var level = growthService.saveLevel(new Level(null, "FilterLevel", 1, 100L, 90, null, null, 1));
        var group = userService.saveGroup(new Group(null, "FilterGroup", null, 1));
        var firstTag = userService.saveTag(new Tag(null, "FirstTag", null));
        var secondTag = userService.saveTag(new Tag(null, "SecondTag", null));
        userService.assignGroup(id, group);
        userService.assignGroup(other, group);
        growthService.assignLevel(id, level, "USER-FILTER-L1", "等级");
        growthService.assignLevel(other, level, "USER-FILTER-L2", "等级");
        userService.assignTags(id, List.of(firstTag, secondTag));
        userService.assignTags(other, List.of(secondTag));
        sql("UPDATE member_user SET nickname='会员一',email='one@shop.test',create_time='2026-10-30 10:00:00',login_date='2026-10-30 10:00:00' WHERE id=" + id);
        sql("UPDATE member_user SET nickname='会员二',email='two@shop.test',create_time='2026-10-30 11:00:00',login_date='2026-10-30 11:00:00' WHERE id=" + other);
        var start = LocalDateTime.of(2026, 10, 30, 10, 0);
        var end = start.plusHours(1);
        var query = new com.lemnt.member.application.user.executor.MemberPageQueryExe(memberUserGateway);
        var first = query.execute(new com.lemnt.member.application.user.qry.MemberPageQry("138", " shop.test ", "会员", level, group,
                List.of(firstTag, secondTag, firstTag), start, end, start, end, 1, 1));
        var second = query.execute(new com.lemnt.member.application.user.qry.MemberPageQry("138", "shop.test", "会员", level, group,
                List.of(firstTag, secondTag), start, end, start, end, 2, 1));
        assertEquals(2, first.getTotal());
        assertEquals(2, second.getTotal());
        assertEquals(other, first.getRecords().get(0).id());
        assertEquals(id, second.getRecords().get(0).id());
        assertEquals(1, query.execute(new com.lemnt.member.application.user.qry.MemberPageQry(null, null, null, null, null,
                List.of(firstTag), null, null, null, null, 1, 20)).getTotal());
        assertEquals(2, query.execute(new com.lemnt.member.application.user.qry.MemberPageQry(null, null, null, null, null,
                List.of(secondTag), null, null, null, null, 1, 20)).getTotal());
        assertEquals(1, query.execute(new com.lemnt.member.application.user.qry.MemberPageQry(null, null, null, null, null,
                null, end, null, null, null, 1, 20)).getTotal());
        assertEquals(1, query.execute(new com.lemnt.member.application.user.qry.MemberPageQry(null, null, null, null, null,
                null, null, null, null, start, 1, 20)).getTotal());
    }

    /** 空标签不筛选，NULL登录时间不匹配时间范围，非法条件在应用层拒绝。 */
    @Test
    void memberUserFilterHandlesEmptyTagsUnknownLoginAndInvalidValues() {
        var query = new com.lemnt.member.application.user.executor.MemberPageQueryExe(memberUserGateway);
        var now = memberUserGateway.now();
        assertEquals(2, query.execute(new com.lemnt.member.application.user.qry.MemberPageQry("138", 1, 20)).getTotal());
        assertEquals(2, query.execute(new com.lemnt.member.application.user.qry.MemberPageQry(null, null, null, null, null,
                List.of(), null, null, null, null, 1, 20)).getTotal());
        assertEquals(0, query.execute(new com.lemnt.member.application.user.qry.MemberPageQry(null, null, null, null, null,
                List.of(Long.MAX_VALUE), null, null, null, null, 1, 20)).getTotal());
        assertEquals(0, query.execute(new com.lemnt.member.application.user.qry.MemberPageQry(null, null, null, null, null,
                null, now.minusDays(1), null, null, null, 1, 20)).getTotal());
        assertBusinessException(400, () -> query.execute(new com.lemnt.member.application.user.qry.MemberPageQry(null, null, null, 0L, null,
                null, null, null, null, null, 1, 20)));
        assertBusinessException(400, () -> query.execute(new com.lemnt.member.application.user.qry.MemberPageQry(null, null, null, null, null,
                List.of(0L), null, null, null, null, 1, 20)));
        assertBusinessException(400, () -> query.execute(new com.lemnt.member.application.user.qry.MemberPageQry(null, null, null, null, null,
                null, now, now.minusDays(1), null, null, 1, 20)));
        assertBusinessException(400, () -> query.execute(new com.lemnt.member.application.user.qry.MemberPageQry(null, null, null, null, null,
                null, null, null, now, now.minusDays(1), 1, 20)));
    }
    /** 后台分页和详情读取相同当前关系，改名即时返显且基础投影不被变异。 */
    @Test
    void memberRelationsAppearInPageAndDetailAndFollowCurrentMetadata() {
        var level = growthService.saveLevel(new Level(null, "Gold", 1, 100L, 90, null, null, 1));
        var group = userService.saveGroup(new Group(null, "VIP", null, 1));
        var tag = userService.saveTag(new Tag(null, "Active", null));
        growthService.assignLevel(id, level, "RELATION-LEVEL", "分配");
        userService.assignGroup(id, group);
        userService.assignTags(id, List.of(tag));
        var relations = new com.lemnt.member.application.user.service.MemberUserRelationQueryService(
                memberLevelGateway, memberGroupGateway, memberTagGateway, memberTagLinkGateway);
        var baseQuery = new com.lemnt.member.application.user.executor.MemberDetailQueryExe(memberUserGateway);
        var base = baseQuery.execute(new com.lemnt.member.application.user.qry.MemberDetailQry(id));
        var detail = new com.lemnt.member.application.user.executor.MemberUserDetailQueryExe(baseQuery, relations);
        var page = new com.lemnt.member.application.user.executor.MemberUserPageQueryExe(
                new com.lemnt.member.application.user.executor.MemberPageQueryExe(memberUserGateway), relations)
                .execute(new com.lemnt.member.application.user.qry.MemberPageQry(null, 1, 20));
        var mine = page.getRecords().stream().filter(v -> id.equals(v.id())).findFirst().orElseThrow();
        var read = detail.execute(new com.lemnt.member.application.user.qry.MemberDetailQry(id));
        assertEquals(List.of(tag), read.tagIds());
        assertEquals(List.of("Active"), read.tagNames());
        assertEquals("Gold", read.levelName());
        assertEquals("VIP", read.groupName());
        assertEquals(read, mine);
        assertNull(base.tagIds());
        assertNull(base.levelName());
        var unrelated = page.getRecords().stream().filter(v -> other.equals(v.id())).findFirst().orElseThrow();
        assertEquals(List.of(), unrelated.tagIds());
        assertEquals(List.of(), unrelated.tagNames());
        assertNull(unrelated.levelName());
        growthService.saveLevel(new Level(level, "NewGold", 1, 100L, 90, null, null, 1));
        userService.saveGroup(new Group(group, "NewVIP", null, 1));
        userService.saveTag(new Tag(tag, "NewActive", null));
        var renamed = detail.execute(new com.lemnt.member.application.user.qry.MemberDetailQry(id));
        assertEquals("NewGold", renamed.levelName());
        assertEquals("NewVIP", renamed.groupName());
        assertEquals(List.of("NewActive"), renamed.tagNames());
        assertEquals("管理员调整为：Gold", memberLevelRecordGateway.levelRecords(id, 0, 20).get(0).description());
        userService.assignTags(id, List.of());
        userService.assignGroup(id, null);
        growthService.assignLevel(id, null, "RELATION-CANCEL", "清空");
        var cleared = detail.execute(new com.lemnt.member.application.user.qry.MemberDetailQry(id));
        assertEquals(List.of(), cleared.tagIds());
        assertNull(cleared.levelName());
        assertNull(cleared.groupName());
        assertNull(detail.execute(new com.lemnt.member.application.user.qry.MemberDetailQry(Long.MAX_VALUE)));
    }
    /** 后台改号仅更新资料，不覆盖密码、资产、关联或登录审计。 */
    @Test
    void webProfileCanChangeMobileWithoutOverwritingMemberState() {
        var group = userService.saveGroup(new Group(null, "ProfileGroup", null, 1));
        var tag = userService.saveTag(new Tag(null, "ProfileTag", null));
        userService.assignGroup(id, group);
        userService.assignTags(id, List.of(tag));
        growthService.adjustAsset(id, "POINT", 5L, "profile-points", "测试");
        memberUserGateway.updatePassword(id, "unchanged-hash");
        var loginTime = java.time.LocalDateTime.of(2026, 10, 30, 12, 0);
        memberUserGateway.recordLogin(id, loginTime, "127.0.0.1");
        var before = memberUserGateway.findById(id);
        assertTrue(userService.updateUser(new com.lemnt.member.application.user.cmd.MemberUserUpdateCmd(
                id, "Updated", "edit@example.com", null, 1, null, 1, "备注", "姓名", 110101, "+8613800000099")));
        var after = memberUserGateway.findById(id);
        assertNull(memberUserGateway.findByMobile(before.mobile()));
        assertEquals(id, memberUserGateway.findByMobile("+8613800000099").id());
        assertEquals("Updated", after.nickname());
        assertEquals(before.points(), after.points());
        assertEquals(before.experience(), after.experience());
        assertEquals(before.levelId(), after.levelId());
        assertEquals(group, after.groupId());
        assertEquals(List.of(tag), memberTagLinkGateway.memberTags(id));
        assertEquals("unchanged-hash", memberUserGateway.findPasswordByMemberId(id));
        assertEquals(loginTime, after.loginDate());
        assertEquals("127.0.0.1", after.loginIp());
        // 同一会员继续使用当前手机号/邮箱，不应误判占用。
        assertTrue(userService.updateUser(new com.lemnt.member.application.user.cmd.MemberUserUpdateCmd(
                id, "Updated", "edit@example.com", null, 1, null, 1, null, null, null, "+8613800000099")));
        // 旧后台客户端及App都不能把手机号写空。
        userService.updateUser(new com.lemnt.member.application.user.cmd.MemberUserUpdateCmd(
                id, "Legacy", "edit@example.com", null, null, null, null, null, null, null, null));
        userService.profileUpdate(id, "App", "edit@example.com", null, null, null, null);
        assertEquals("+8613800000099", memberUserGateway.findById(id).mobile());
    }

    /** 占用或非法手机号失败时，其他资料不能出现部分保存。 */
    @Test
    void occupiedOrInvalidCredentialsLeaveOriginalProfileIntact() {
        userService.profileUpdate(other, "two", "occupied@example.com", null, null, null, null);
        var before = memberUserGateway.findById(id);
        assertBusinessException(409, () -> userService.updateUser(
                new com.lemnt.member.application.user.cmd.MemberUserUpdateCmd(id, "不得保存", null,
                        null, null, null, 0, "不得保存", null, null, memberUserGateway.findById(other).mobile())));
        assertBusinessException(409, () -> userService.updateUser(
                new com.lemnt.member.application.user.cmd.MemberUserUpdateCmd(id, "不得保存", "occupied@example.com",
                        null, null, null, 0, "不得保存", null, null, "13800000099")));
        assertBusinessException(400, () -> userService.updateUser(
                new com.lemnt.member.application.user.cmd.MemberUserUpdateCmd(id, "不得保存", null,
                        null, null, null, 0, "不得保存", null, null, "")));
        assertEquals(before, memberUserGateway.findById(id));
        assertNull(memberUserGateway.findByMobile("13800000099"));
    }

    /** 数据库唯一约束仍兜底同号码并发竞争，统一转换业务冲突。 */
    @Test
    void databaseUniqueConstraintRejectsCredentialCollision() {
        var old = memberUserGateway.findById(id);
        assertBusinessException(409, () -> memberUserGateway.updateProfile(new Member(
                id, "不得保存", memberUserGateway.findById(other).mobile(), null, null, 0,
                old.points(), old.experience(), old.levelId(), old.groupId(), null, null, old.createTime())));
        assertEquals(old, memberUserGateway.findById(id));
    }

    /** 仅后台明确保存停用状态时同步要求auth撤销会话。 */
    @Test
    void disablingMemberRequestsRemoteSessionRevocationOnlyFromWebUpdate() {
        userService.updateUser(new com.lemnt.member.application.user.cmd.MemberUserUpdateCmd(
                id, "disabled", null, null, null, null, 0, null, null, null, null));
        assertEquals(0, memberUserGateway.findById(id).status());
        org.mockito.Mockito.verify(memberAuthGateway).revokeSessions(id);
        userService.updateUser(new com.lemnt.member.application.user.cmd.MemberUserUpdateCmd(
                id, "enabled", null, null, null, null, 1, null, null, null, null));
        userService.profileUpdate(id, "App", null, null, null, null, null);
        org.mockito.Mockito.verifyNoMoreInteractions(memberAuthGateway);
    }

    /** 认证中心撤销失败时，停用状态、改号和普通资料共同回滚。 */
    @Test
    void failedRemoteRevocationRollsBackEntireMemberUpdate() {
        var before = memberUserGateway.findById(id);
        org.mockito.Mockito.doThrow(new IllegalStateException("auth不可用"))
                .when(memberAuthGateway).revokeSessions(id);
        assertThrows(IllegalStateException.class, () -> userService.updateUser(
                new com.lemnt.member.application.user.cmd.MemberUserUpdateCmd(
                        id, "不得保存", null, null, null, null, 0, "不得保存", null, null, "13800000099")));
        assertEquals(before, memberUserGateway.findById(id));
        assertNull(memberUserGateway.findByMobile("13800000099"));
    }

    /** 等级相同按源规则无操作，配置后来修改或停用不能重置会员经验。 */
    @Test
    void assigningCurrentLevelKeepsExperienceAndCreatesNoRecords() throws Exception {
        var level = growthService.saveLevel(new Level(null, "Gold", 1, 100L, 90, null, null, 1));
        growthService.adjustAsset(id, "EXPERIENCE", 150, "SAME-LEVEL-SEED", "经验奖励");
        var before = memberUserGateway.findById(id);
        long experienceRecords = count("member_experience_record");
        long levelRecords = memberLevelRecordGateway.levelRecordCount(id);
        assertTrue(growthService.assignLevel(id, level, "SAME-LEVEL-NEW", "保持等级"));
        assertEquals(before, memberUserGateway.findById(id));
        assertEquals(experienceRecords, count("member_experience_record"));
        assertEquals(levelRecords, memberLevelRecordGateway.levelRecordCount(id));
        sql("UPDATE member_level SET experience=200,status=0 WHERE id=" + level);
        assertTrue(growthService.assignLevel(id, level, "SAME-LEVEL-DISABLED", "保持等级"));
        assertEquals(before, memberUserGateway.findById(id));
        assertEquals(experienceRecords, count("member_experience_record"));
        assertEquals(levelRecords, memberLevelRecordGateway.levelRecordCount(id));
    }

    /** 没有等级的会员再次取消等级，不应抹掉已有经验或插入占位记录。 */
    @Test
    void cancellingAlreadyUnassignedLevelKeepsExperienceAndDoesNotConsumeRequest() throws Exception {
        growthService.adjustAsset(id, "EXPERIENCE", 50, "UNASSIGNED-SEED", "经验奖励");
        var before = memberUserGateway.findById(id);
        assertNull(before.levelId());
        assertTrue(growthService.assignLevel(id, null, "UNASSIGNED-NOOP", "保持无等级"));
        assertEquals(before, memberUserGateway.findById(id));
        assertEquals(1, count("member_experience_record"));
        assertEquals(0, memberLevelRecordGateway.levelRecordCount(id));
        var level = growthService.saveLevel(new Level(null, "Gold", 1, 100L, 90, null, null, 1));
        assertTrue(growthService.assignLevel(id, level, "UNASSIGNED-NOOP", "实际调整"));
        assertEquals(level, memberUserGateway.findById(id).levelId());
        assertEquals(100L, memberUserGateway.findById(id).experience());
        assertEquals(2, count("member_experience_record"));
        assertEquals(1, memberLevelRecordGateway.levelRecordCount(id));
    }

    /** 同等级并发提交在原会员行锁内判断，不重置经验、不重复写流水。 */
    @Test
    void concurrentCurrentLevelRequestsAreNoops() throws Exception {
        var level = growthService.saveLevel(new Level(null, "Gold", 1, 100L, 90, null, null, 1));
        growthService.adjustAsset(id, "EXPERIENCE", 150, "CONCURRENT-LEVEL-SEED", "经验奖励");
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> growthService.assignLevel(id, level, "SAME-CONCURRENT-1", "保持等级"));
            var second = pool.submit(() -> growthService.assignLevel(id, level, "SAME-CONCURRENT-2", "保持等级"));
            assertTrue(first.get());
            assertTrue(second.get());
        } finally {
            pool.shutdownNow();
        }
        assertEquals(150L, memberUserGateway.findById(id).experience());
        assertEquals(1, count("member_experience_record"));
        assertEquals(1, memberLevelRecordGateway.levelRecordCount(id));
    }

    /** 默认切换只更新真实变更的本人地址，保留原创建审计。 */
    @Test
    void addressAuditTracksOnlyChangedDefaultsAndKeepsOtherOwnersUntouched() throws Exception {
        var mapper = template.getMapper(MemberAddressMapper.class);
        com.lemnt.common.security.context.SecurityContextHolder.setUserName("address-creator");
        try {
            var first = addressService.saveAddress(address(id, null, true));
            var next = addressService.saveAddress(address(id, null, false));
            var ordinary = addressService.saveAddress(address(id, null, false));
            var foreign = addressService.saveAddress(address(other, null, true));
            var original = mapper.selectById(first.id());
            assertEquals("address-creator", original.getCreateBy());
            assertNotNull(original.getCreateTime());
            sql("UPDATE member_address SET update_time='2020-01-01 00:00:00',update_by='old-editor'");
            com.lemnt.common.security.context.SecurityContextHolder.setUserName("address-editor");
            addressService.saveAddress(address(id, next.id(), true));
            var cleared = mapper.selectById(first.id());
            var selected = mapper.selectById(next.id());
            assertFalse(cleared.getDefaultStatus());
            assertEquals("address-editor", cleared.getUpdateBy());
            assertEquals("address-editor", selected.getUpdateBy());
            assertEquals(original.getCreateBy(), cleared.getCreateBy());
            assertEquals(original.getCreateTime(), cleared.getCreateTime());
            assertTrue(cleared.getUpdateTime().isAfter(java.time.LocalDateTime.of(2020, 1, 1, 0, 0)));
            assertEquals("old-editor", mapper.selectById(ordinary.id()).getUpdateBy());
            assertEquals("old-editor", mapper.selectById(foreign.id()).getUpdateBy());
            assertTrue(mapper.selectById(foreign.id()).getDefaultStatus());
            assertEquals(next.id(), memberAddressGateway.defaultAddress(id).id());
            assertEquals(foreign.id(), memberAddressGateway.defaultAddress(other).id());
        } finally {
            com.lemnt.common.security.context.SecurityContextHolder.remove();
        }
    }

    /** 旧属主列及未知审计迁移后保留原归属、默认标志、编号和外键。 */
    @Test
    void addressUpgradePreservesLegacyRowsAndLeavesUnknownAuditNull() throws Exception {
        sql("DROP TABLE member_address");
        sql("CREATE TABLE member_address(id BIGINT AUTO_INCREMENT PRIMARY KEY,member_id BIGINT NOT NULL,name VARCHAR(80) NOT NULL,mobile VARCHAR(24) NOT NULL,area_id BIGINT NOT NULL,detail_address VARCHAR(500) NOT NULL,default_status BOOLEAN NOT NULL DEFAULT FALSE,FOREIGN KEY(member_id) REFERENCES member_user(id),KEY idx_member_address_owner(member_id))");
        sql("INSERT INTO member_address(id,member_id,name,mobile,area_id,detail_address,default_status) VALUES(900," + id + ",'old','13800000003',110101,'legacy',true)");
        ScriptUtils.executeSqlScript(schema, new ClassPathResource("member-sql/upgrade/address_owner_audit.sql"));
        var mapper = template.getMapper(MemberAddressMapper.class);
        var old = mapper.selectById(900L);
        assertEquals(id, old.getUserId());
        assertTrue(old.getDefaultStatus());
        assertEquals("legacy", old.getDetailAddress());
        assertNull(old.getCreateBy());
        assertNull(old.getCreateTime());
        assertNull(old.getUpdateBy());
        assertNull(old.getUpdateTime());
        assertNull(memberAddressGateway.address(other, 900L));
        assertEquals(900L, memberAddressGateway.defaultAddress(id).id());
        var added = addressService.saveAddress(address(id, null, false));
        assertNotEquals(900L, added.id());
        assertNotNull(mapper.selectById(added.id()).getCreateTime());
        assertThrows(Exception.class, () -> memberAddressGateway.saveAddress(address(Long.MAX_VALUE, null, false)));
    }

    /** 删除缺失/他人地址报相同错误，删除默认项不擅自把其他项设为默认。 */
    @Test
    void deletingAddressChecksOwnerAndDoesNotInventAnotherDefault() {
        var ordinary = addressService.saveAddress(address(id, null, false));
        assertNull(memberAddressGateway.defaultAddress(id));
        var selected = addressService.saveAddress(address(id, null, true));
        assertBusinessException(404, () -> addressService.deleteAddress(other, selected.id()));
        assertBusinessException(404, () -> addressService.deleteAddress(id, Long.MAX_VALUE));
        assertTrue(addressService.deleteAddress(id, selected.id()));
        assertBusinessException(404, () -> addressService.deleteAddress(id, selected.id()));
        assertNull(memberAddressGateway.defaultAddress(id));
        assertNotNull(memberAddressGateway.address(id, ordinary.id()));
        assertFalse(memberAddressGateway.address(id, ordinary.id()).defaultStatus());
    }

    /** 保存失败时，先前默认地址及其更新审计必须共同回滚。 */
    @Test
    void addressWriteFailureRollsBackDefaultAndAuditChanges() throws Exception {
        var first = addressService.saveAddress(address(id, null, true));
        var mapper = template.getMapper(MemberAddressMapper.class);
        var before = mapper.selectById(first.id());
        sql("ALTER TABLE member_address ADD CONSTRAINT address_test_failure CHECK (detail_address <> 'trigger-rollback')");
        assertThrows(Exception.class, () -> addressService.saveAddress(new Address(null, id,
                "收件人", "13800000003", 110101L, "trigger-rollback", true)));
        assertEquals(before, mapper.selectById(first.id()));
        assertEquals(1, memberAddressGateway.addresses(id).size());
        assertEquals(first, memberAddressGateway.defaultAddress(id));
    }

    /** 真实H2保存和默认查询贯通HTTP，本人上下文不接受外部属主覆盖。 */
    @Test
    void addressHttpUsesOwnerContextAndUnifiedMissingResponse() throws Exception {
        var controller = new com.lemnt.member.adapter.app.address.AppMemberAddressController(
                new com.lemnt.member.application.address.executor.MemberAddressDeleteCmdExe(addressService),
                new com.lemnt.member.application.address.executor.MemberAddressDetailQueryExe(memberAddressGateway),
                new com.lemnt.member.application.address.executor.MemberAddressListQueryExe(memberAddressGateway),
                new com.lemnt.member.application.address.executor.MemberAddressSaveCmdExe(addressService),
                new com.lemnt.member.application.address.executor.MemberAddressDefaultQueryExe(memberAddressGateway));
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor())
                .setControllerAdvice(new com.lemnt.common.security.handler.GlobalExceptionHandler()).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/app/addresses")
                .header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, id.toString())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"memberId\":" + other + ",\"name\":\"HTTP\",\"mobile\":\"13800000003\",\"areaId\":110101,\"detailAddress\":\"HTTP address\",\"defaultStatus\":true}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.memberId").value(id.intValue()));
        assertTrue(memberAddressGateway.addresses(other).isEmpty());
        var selected = memberAddressGateway.defaultAddress(id);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/app/addresses/default")
                .param("memberId", other.toString())
                .header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, id.toString()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.id").value(selected.id().intValue()));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/app/addresses/" + selected.id())
                .header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, other.toString()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value(404));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/app/addresses/" + selected.id())
                .header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, id.toString()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data").value(true));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/app/addresses/default")
                .header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, id.toString()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data").doesNotExist());
    }

}
