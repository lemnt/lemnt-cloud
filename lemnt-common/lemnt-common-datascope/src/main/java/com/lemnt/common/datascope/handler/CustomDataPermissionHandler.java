package com.lemnt.common.datascope.handler;

import com.lemnt.common.core.constant.UserConstants;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.datascope.annotation.DataScope;
import com.lemnt.common.datascope.enums.DataScopeEnum;
import com.lemnt.common.security.model.SecurityRole;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.model.SecurityUserDetail;
import com.lemnt.common.security.utils.SecurityUtils;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import lombok.SneakyThrows;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限控制器
 *
 * <p>按当前登录用户角色的数据范围（sys_role.data_scope）动态改写查询 SQL 的 WHERE 条件，
 * 各数据范围的含义见 {@link DataScopeEnum}。用户有多个角色时，取各数据范围的并集。</p>
 *
 * <p>部门维度与用户维度的过滤分别依赖注解上的 {@link DataScope#deptAlias()} 与
 * {@link DataScope#userAlias()}。未配置对应别名时不生成该维度的条件：此时无法确定列
 * 属于哪张表，强行拼接裸列名会在多表关联查询中造成列名歧义。</p>
 *
 * <p>注意：{@link DataScope#permission()} 当前未参与过滤计算。</p>
 *
 * @author wangxx
 * @version 1.0.0
 * @date 2025/7/28 21:52
 */
public class CustomDataPermissionHandler implements DataPermissionHandler {

    /**
     * 部门表名，用于“本部门及以下”的上下级部门子查询
     */
    private static final String DEPT_TABLE = "sys_dept";

    /**
     * 角色部门关联表名，用于“自定义数据权限”的授权部门子查询
     */
    private static final String ROLE_DEPT_TABLE = "sys_role_dept";

    /**
     * 部门ID列名
     */
    private static final String DEPT_ID_COLUMN = "dept_id";

    @Override
    @SneakyThrows
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        Class<?> clazz = Class.forName(mappedStatementId.substring(0, mappedStatementId.lastIndexOf(StringPool.DOT)));
        String methodName = mappedStatementId.substring(mappedStatementId.lastIndexOf(StringPool.DOT) + 1);
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                DataScope annotation = method.getAnnotation(DataScope.class);
                // 如果没有注解，直接返回
                if (Objects.isNull(annotation)) {
                    return where;
                }
                SecurityUser loginUser = SecurityUtils.getLoginUser();
                if (Objects.isNull(loginUser)) {
                    throw new ServiceException("数据权限过滤失败：未获取到登录用户上下文");
                }
                // 超级管理员，直接返回
                if (SecurityUtils.isAdmin(loginUser.getUserId())) {
                    return where;
                }
                return dataScopeFilter(annotation, where, loginUser);
            }
        }
        return where;
    }

    /**
     * 按角色数据范围生成过滤条件
     *
     * @param annotation 数据权限注解
     * @param where      原查询条件，可为 null
     * @param loginUser  当前登录用户
     * @return 追加数据权限后的查询条件
     */
    @SneakyThrows
    private Expression dataScopeFilter(DataScope annotation, Expression where, SecurityUser loginUser) {
        SecurityUserDetail user = loginUser.getDetail();
        if (Objects.isNull(user) || Objects.isNull(user.getRoles()) || user.getRoles().isEmpty()) {
            return where;
        }
        // 停用角色不参与数据权限计算
        List<SecurityRole> roles = user.getRoles().stream()
                .filter(role -> !Objects.equals(role.getStatus(), UserConstants.ROLE_DISABLE))
                .collect(Collectors.toList());
        if (roles.isEmpty()) {
            return where;
        }
        // 任一角色拥有全部数据权限，无需过滤
        boolean hasAllScope = roles.stream()
                .map(SecurityRole::getDataScope)
                .map(DataScopeEnum::getDataScopeEnum)
                .anyMatch(scope -> Objects.equals(DataScopeEnum.ALL, scope));
        if (hasAllScope) {
            return where;
        }
        // 多个角色的数据范围取并集，LinkedHashSet 保证生成的 SQL 条件顺序稳定
        Set<DataScopeEnum> scopes = roles.stream()
                .map(SecurityRole::getDataScope)
                .map(DataScopeEnum::getDataScopeEnum)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<String> conditions = new ArrayList<>();
        for (DataScopeEnum scope : scopes) {
            switch (scope) {
                case CUSTOM:
                    appendCustomCondition(conditions, annotation, roles);
                    break;
                case DEPT:
                    appendDeptCondition(conditions, annotation, user);
                    break;
                case DEPT_AND_CHILD:
                    appendDeptAndChildCondition(conditions, annotation, user);
                    break;
                case SELF:
                    appendSelfCondition(conditions, annotation, user);
                    break;
                default:
                    break;
            }
        }
        // 数据范围无法用当前注解表达，保持原查询条件不做过滤
        if (conditions.isEmpty()) {
            return where;
        }
        Expression appendExpression = CCJSqlParserUtil.parseCondExpression("(" + String.join(" OR ", conditions) + ")");
        if (Objects.isNull(where)) {
            return appendExpression;
        }
        return new AndExpression(where, appendExpression);
    }

    /**
     * 自定义数据权限：按角色在 sys_role_dept 中授权的部门过滤
     */
    private void appendCustomCondition(List<String> conditions, DataScope annotation, List<SecurityRole> roles) {
        String deptColumn = resolveDeptColumn(annotation);
        if (Objects.isNull(deptColumn)) {
            return;
        }
        String roleIds = roles.stream()
                .map(SecurityRole::getRoleId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(StringPool.COMMA));
        if (StringUtils.isBlank(roleIds)) {
            return;
        }
        conditions.add(deptColumn + " IN (SELECT " + DEPT_ID_COLUMN + " FROM " + ROLE_DEPT_TABLE
                + " WHERE role_id IN (" + roleIds + "))");
    }

    /**
     * 本部门数据权限
     */
    private void appendDeptCondition(List<String> conditions, DataScope annotation, SecurityUserDetail user) {
        String deptColumn = resolveDeptColumn(annotation);
        if (Objects.isNull(deptColumn) || Objects.isNull(user.getDeptId())) {
            return;
        }
        conditions.add(deptColumn + " = " + user.getDeptId());
    }

    /**
     * 本部门及以下数据权限：命中本部门自身，或祖级列表中含本部门的部门
     */
    private void appendDeptAndChildCondition(List<String> conditions, DataScope annotation, SecurityUserDetail user) {
        String deptColumn = resolveDeptColumn(annotation);
        if (Objects.isNull(deptColumn) || Objects.isNull(user.getDeptId())) {
            return;
        }
        conditions.add(deptColumn + " IN (SELECT " + DEPT_ID_COLUMN + " FROM " + DEPT_TABLE
                + " WHERE " + DEPT_ID_COLUMN + " = " + user.getDeptId()
                + " OR FIND_IN_SET(" + user.getDeptId() + ", ancestors))");
    }

    /**
     * 仅本人数据权限
     */
    private void appendSelfCondition(List<String> conditions, DataScope annotation, SecurityUserDetail user) {
        String userColumn = resolveUserColumn(annotation);
        if (Objects.isNull(userColumn) || Objects.isNull(user.getUserId())) {
            return;
        }
        conditions.add(userColumn + " = " + user.getUserId());
    }

    /**
     * 解析部门过滤列名
     *
     * @return 带表别名的部门列名，未配置 deptAlias 时返回 null
     */
    private String resolveDeptColumn(DataScope annotation) {
        String deptAlias = annotation.deptAlias();
        return StringUtils.isNotBlank(deptAlias) ? (deptAlias + StringPool.DOT + DEPT_ID_COLUMN) : null;
    }

    /**
     * 解析用户过滤列名
     *
     * @return 带表别名的用户列名，未配置 userAlias 时返回 null
     */
    private String resolveUserColumn(DataScope annotation) {
        String userAlias = annotation.userAlias();
        return StringUtils.isNotBlank(userAlias) ? (userAlias + StringPool.DOT + annotation.userIdColumnName()) : null;
    }
}