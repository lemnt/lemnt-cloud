package com.lemnt.auth.domain.user.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户角色（值对象）
 * <p>
 * 不可变、无身份标识，由角色属性定义其值。
 * 当前因 MapStruct 映射需要保留 setter，调用方应将其视为值对象使用。
 * </p>
 *
 * @author wangxx
 */
@Data
@Accessors(chain = true)
public class UserInfoRole implements Serializable {
    @Serial
    private static final long serialVersionUID = -1397036721959406735L;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色权限
     */
    private String roleKey;

    /**
     * 角色排序
     */
    private Integer roleSort;

    /**
     * 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限）
     */
    private String dataScope;

    /**
     * 角色状态（1正常 2停用）
     */
    private String status;
}
