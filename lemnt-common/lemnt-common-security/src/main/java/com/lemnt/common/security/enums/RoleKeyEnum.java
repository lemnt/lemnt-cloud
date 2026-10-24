package com.lemnt.common.security.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 角色权限标识枚举
 *
 * @author wangxx
 */
@Getter
public enum RoleKeyEnum {

    ADMIN("超级管理员", "admin", 1, true),
    SYSTEM("系统管理员", "system", 2, true),
    COMMON("普通用户", "common", 99, false),
    ;

    private final String roleName;
    private final String roleKey;
    private final int sort;
    private final boolean builtIn;

    RoleKeyEnum(String roleName, String roleKey, int sort, boolean builtIn) {
        this.roleName = roleName;
        this.roleKey = roleKey;
        this.sort = sort;
        this.builtIn = builtIn;
    }

    private static final Map<String, RoleKeyEnum> TYPE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(RoleKeyEnum::getRoleKey, e -> e));

    public static RoleKeyEnum getByRoleKey(String roleKey) {
        if (Objects.isNull(roleKey)) {
            return null;
        }
        return TYPE_MAP.get(roleKey);
    }
}
