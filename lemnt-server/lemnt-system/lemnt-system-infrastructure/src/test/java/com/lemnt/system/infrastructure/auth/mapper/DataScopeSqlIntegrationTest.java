package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.common.core.constant.UserConstants;
import com.lemnt.common.datascope.config.DataScopeAutoConfig;
import com.lemnt.common.mybatis.config.MybatisPlusConfig;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.security.model.SecurityRole;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.model.SecurityUserDetail;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.domain.auth.model.User;
import com.lemnt.system.domain.org.model.Dept;
import com.lemnt.system.infrastructure.auth.dataobject.RoleDO;
import com.lemnt.system.infrastructure.org.mapper.DeptMapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSession;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 生产 Mapper/XML + 实际数据权限和分页拦截器 + H2 SQL 执行验证。 */
public class DataScopeSqlIntegrationTest {
    private SqlSession session;
    private UserMapper users;
    private RoleMapper roles;
    private DeptMapper departments;

    @BeforeEach
    void initializeDatabaseAndMappers() throws Exception {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:scope_" + UUID.randomUUID() + ";MODE=MySQL");
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        DataScopeAutoConfig scopeConfig = new DataScopeAutoConfig();
        var interceptor = new MybatisPlusConfig().mybatisPlusInterceptor(Optional.of(
                scopeConfig.dataPermissionInterceptor(scopeConfig.dataPermissionHandler())));
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(configuration);
        factory.setPlugins(interceptor);
        factory.setMapperLocations(new ClassPathResource("mapper/UserMapper.xml"),
                new ClassPathResource("mapper/RoleMapper.xml"), new ClassPathResource("mapper/DeptMapper.xml"));
        session = factory.getObject().openSession();
        try (var statement = session.getConnection().createStatement();
             var input = getClass().getResourceAsStream("/datascope-fixture.sql")) {
            assertThat(input).isNotNull();
            String sql = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            for (String command : sql.split(";")) {
                if (!command.isBlank()) {
                    statement.execute(command);
                }
            }
        }
        users = session.getMapper(UserMapper.class);
        roles = session.getMapper(RoleMapper.class);
        departments = session.getMapper(DeptMapper.class);
    }

    @AfterEach
    void closeSessionAndContext() {
        SecurityContextHolder.remove();
        if (session != null) {
            session.close();
        }
    }

    @ParameterizedTest
    @CsvSource({"3, '100,101'", "5, 100", "2, 200", "4, '100,101,110'", "1, '100,101,110,200'"})
    void userPageShouldApplyEachScopeToRecordsAndCount(String scope, String expected) {
        login(100L, role(7L, scope));

        var page = users.selectPage(new Page<User>(1, 10), new User());

        List<Long> ids = Arrays.stream(expected.split(",")).map(Long::valueOf).toList();
        assertThat(page.getRecords()).extracting(User::getUserId).containsExactlyInAnyOrderElementsOf(ids);
        assertThat(page.getTotal()).isEqualTo(ids.size());
    }

    @Test
    void paginationCountShouldNotIncludeOtherDepartments() {
        login(100L, role(7L, "3"));

        var page = users.selectPage(new Page<User>(1, 1), new User());

        assertThat(page.getRecords()).hasSize(1).allSatisfy(user -> assertThat(user.getDeptId()).isEqualTo(10L));
        assertThat(page.getTotal()).isEqualTo(2);
    }

    @Test
    void allocatedAndUnallocatedQueriesShouldBothBeScoped() {
        login(100L, role(7L, "3"));
        User query = new User().setRoleId(7L);

        var allocated = users.selectAllocatedList(new Page<>(1, 10), query);
        var unallocated = users.selectUnallocatedList(new Page<>(1, 10), query);

        assertThat(allocated.getRecords()).extracting("userId").containsExactly(100L);
        assertThat(allocated.getTotal()).isEqualTo(1);
        assertThat(unallocated.getRecords()).extracting("userId").containsExactly(101L);
        assertThat(unallocated.getTotal()).isEqualTo(1);
    }

    @Test
    void rolePageListAndAllQueriesShouldReadMapperAnnotations() {
        login(100L, role(7L, "3"));

        var page = roles.selectPage(new Page<RoleDO>(1, 10), new Role());

        assertThat(page.getRecords()).extracting(RoleDO::getRoleId).containsExactly(7L);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(roles.selectRoleList(new Role())).extracting(RoleDO::getRoleId).containsExactly(7L);
        assertThat(roles.selectRoleAll()).extracting(RoleDO::getRoleId).containsExactly(7L);
    }

    @Test
    void departmentQueryShouldBeScoped() {
        login(100L, role(7L, "3"));

        assertThat(departments.selectDeptList(new Dept())).extracting("deptId").containsExactly(10L);
    }

    @Test
    void absentOrDisabledRolesShouldReturnNoRecordsAndZeroCount() {
        login(100L);
        var empty = users.selectPage(new Page<User>(1, 10), new User());
        assertThat(empty.getRecords()).isEmpty();
        assertThat(empty.getTotal()).isZero();
        login(100L, role(7L, "1").setStatus(UserConstants.ROLE_DISABLE));
        session.clearCache();
        var disabled = users.selectPage(new Page<User>(1, 10), new User());
        assertThat(disabled.getRecords()).isEmpty();
        assertThat(disabled.getTotal()).isZero();
    }

    @Test
    void scopedQueryWithoutLoginContextShouldFail() {
        assertThatThrownBy(() -> users.selectPage(new Page<User>(1, 10), new User()))
                .hasStackTraceContaining("未获取到登录用户上下文");
    }

    @Test
    void adminShouldSeeAllNonDeletedRecords() {
        login(1L);

        var page = users.selectPage(new Page<User>(1, 10), new User());

        assertThat(page.getRecords()).extracting(User::getUserId).containsExactlyInAnyOrder(100L, 101L, 110L, 200L);
        assertThat(page.getTotal()).isEqualTo(4);
    }

    @Test
    void loginLookupShouldRemainAvailableWithoutUserContext() {
        assertThat(users.selectUserByUserName("outside").getUserId()).isEqualTo(200L);
        assertThat(roles.selectRolePermissionByUserId(200L)).extracting(RoleDO::getRoleId).containsExactly(8L);
    }

    @Test
    void legacySqlFragmentShouldNotOverrideMapperScope() {
        login(100L, role(7L, "3"));
        User query = new User().setUserName("outside").setDataScope(" OR 1=1");

        var page = users.selectPage(new Page<User>(1, 10), query);

        assertThat(page.getRecords()).isEmpty();
        assertThat(page.getTotal()).isZero();
    }

    private static SecurityRole role(Long id, String scope) {
        return new SecurityRole().setRoleId(id).setDataScope(scope).setStatus(UserConstants.ROLE_NORMAL);
    }

    private static void login(Long userId, SecurityRole... roles) {
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, new SecurityUser().setUserId(userId)
                .setDetail(new SecurityUserDetail().setUserId(userId).setDeptId(10L).setRoles(List.of(roles))));
    }

    /** H2 测试库模拟 MySQL FIND_IN_SET，仅用于验证真实部门子查询的执行结果。 */
    public static int findInSet(Long value, String ancestors) {
        return ancestors == null ? 0 : Arrays.asList(ancestors.split(",")).indexOf(String.valueOf(value)) + 1;
    }
}
