package com.lemnt.common.datascope.handler;

import com.lemnt.common.core.constant.UserConstants;
import com.lemnt.common.datascope.annotation.DataScope;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.security.model.SecurityRole;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.model.SecurityUserDetail;

import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 数据权限控制器测试
 *
 * @author wangxx
 */
class CustomDataPermissionHandlerTest {

    /**
     * mappedStatementId 前缀，格式为「全限定类名.方法名」
     */
    private static final String METHOD_PREFIX = TestMapper.class.getName() + ".";

    private final CustomDataPermissionHandler handler = new CustomDataPermissionHandler();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.remove();
    }

    @Test
    @DisplayName("全部数据权限：不做任何过滤")
    void allScopeShouldNotFilter() {
        login(100L, 10L, role(1L, "1"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("超级管理员：不做任何过滤")
    void adminShouldNotFilter() {
        login(1L, 10L, role(1L, "5"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("本部门数据权限：生成部门等值条件")
    void deptScopeShouldFilterByDept() {
        login(100L, 10L, role(1L, "3"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result).isNotNull();
        assertThat(result.toString()).contains("d.dept_id = 10");
    }

    @Test
    @DisplayName("本部门及以下数据权限：生成含 FIND_IN_SET 的部门子查询")
    void deptAndChildScopeShouldFilterByDeptTree() {
        login(100L, 10L, role(1L, "4"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result).isNotNull();
        assertThat(result.toString())
                .contains("sys_dept")
                .contains("FIND_IN_SET(10, ancestors)");
    }

    @Test
    @DisplayName("自定义数据权限：生成按角色授权部门的子查询")
    void customScopeShouldFilterByRoleDept() {
        login(100L, 10L, role(7L, "2"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result).isNotNull();
        assertThat(result.toString())
                .contains("sys_role_dept")
                .contains("role_id IN (7)");
    }

    @Test
    @DisplayName("仅本人数据权限：生成用户等值条件")
    void selfScopeShouldFilterByUser() {
        login(100L, 10L, role(1L, "5"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result).isNotNull();
        assertThat(result.toString()).contains("u.user_id = 100");
    }

    @Test
    @DisplayName("多个角色的数据范围取并集")
    void multipleScopesShouldUnionConditions() {
        login(100L, 10L, role(1L, "3"), role(2L, "5"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result).isNotNull();
        assertThat(result.toString())
                .contains("d.dept_id = 10")
                .contains("u.user_id = 100")
                .contains(" OR ");
    }

    @Test
    @DisplayName("任一角色为全部数据权限时不过滤")
    void anyAllScopeShouldNotFilter() {
        login(100L, 10L, role(1L, "3"), role(2L, "1"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("未标注解的方法不做过滤")
    void withoutAnnotationShouldNotFilter() {
        login(100L, 10L, role(1L, "3"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withoutAnnotation");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("用户无角色时返回零条记录")
    void withoutRoleShouldDeny() {
        login(100L, 10L);

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo("1 = 0");
    }

    @Test
    @DisplayName("角色全部停用时返回零条记录")
    void disabledRoleShouldDeny() {
        SecurityRole disabled = role(1L, "3").setStatus(UserConstants.ROLE_DISABLE);
        login(100L, 10L, disabled);

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo("1 = 0");
    }

    @Test
    @DisplayName("仅本人权限缺少 userAlias 时返回零条记录")
    void selfScopeWithoutUserAliasShouldDeny() {
        login(100L, 10L, role(1L, "5"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withDeptAliasOnly");

        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo("1 = 0");
    }

    @Test
    @DisplayName("部门权限缺少 deptAlias 时返回零条记录")
    void deptScopeWithoutDeptAliasShouldDeny() {
        login(100L, 10L, role(1L, "3"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withUserAliasOnly");

        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo("1 = 0");
    }

    @Test
    @DisplayName("部门权限缺少部门信息时返回零条记录")
    void deptScopeWithoutDeptIdShouldDeny() {
        login(100L, null, role(1L, "3"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo("1 = 0");
    }

    @Test
    @DisplayName("无法识别的数据范围值返回零条记录")
    void unknownScopeShouldDeny() {
        login(100L, 10L, role(1L, "9"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo("1 = 0");
    }

    @Test
    @DisplayName("已有查询条件时，数据权限条件以 AND 追加")
    void shouldAppendToExistingWhere() {
        login(100L, 10L, role(1L, "3"));
        Expression where = parse("u.status = '1'");

        Expression result = handler.getSqlSegment(where, METHOD_PREFIX + "withBothAlias");

        assertThat(result).isNotNull();
        assertThat(result.toString())
                .contains("u.status = '1'")
                .contains("d.dept_id = 10");
    }

    @Test
    @DisplayName("用户详情缺失时返回零条记录")
    void missingDetailShouldDeny() {
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, new SecurityUser().setUserId(100L));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result.toString()).isEqualTo("1 = 0");
    }

    @Test
    @DisplayName("角色列表缺失时返回零条记录")
    void nullRolesShouldDeny() {
        login(100L, 10L);
        com.lemnt.common.security.utils.SecurityUtils.getLoginUser().getDetail().setRoles(null);

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result.toString()).isEqualTo("1 = 0");
    }

    @Test
    @DisplayName("无角色且原条件包含 OR 时，拒绝条件覆盖整个查询")
    void denialShouldConstrainEntireWhere() {
        login(100L, 10L);

        Expression result = handler.getSqlSegment(parse("u.status = '1' OR u.user_id = 200"),
                METHOD_PREFIX + "withBothAlias");

        assertThat(result.toString()).isEqualTo("(u.status = '1' OR u.user_id = 200) AND 1 = 0");
    }

    @Test
    @DisplayName("停用角色的全部权限不能覆盖有效角色的本人权限")
    void disabledAllScopeShouldNotOverrideSelfScope() {
        login(100L, 10L, role(1L, "1").setStatus(UserConstants.ROLE_DISABLE), role(2L, "5"));

        Expression result = handler.getSqlSegment(null, METHOD_PREFIX + "withBothAlias");

        assertThat(result.toString()).contains("u.user_id = 100");
    }

    private static SecurityRole role(Long roleId, String dataScope) {
        return new SecurityRole()
                .setRoleId(roleId)
                .setDataScope(dataScope)
                .setStatus(UserConstants.ROLE_NORMAL);
    }

    private static void login(Long userId, Long deptId, SecurityRole... roles) {
        SecurityUserDetail detail = new SecurityUserDetail()
                .setUserId(userId)
                .setDeptId(deptId)
                .setRoles(List.of(roles));
        SecurityUser loginUser = new SecurityUser()
                .setUserId(userId)
                .setDetail(detail);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
    }

    private static Expression parse(String condition) {
        try {
            return net.sf.jsqlparser.parser.CCJSqlParserUtil.parseCondExpression(condition);
        } catch (Exception e) {
            throw new IllegalStateException("测试条件解析失败：" + condition, e);
        }
    }

    /**
     * 测试用 Mapper，用于承载 {@link DataScope} 注解
     */
    interface TestMapper {

        @DataScope(deptAlias = "d", userAlias = "u")
        void withBothAlias();

        @DataScope(deptAlias = "d")
        void withDeptAliasOnly();

        @DataScope(userAlias = "u")
        void withUserAliasOnly();

        void withoutAnnotation();
    }
}