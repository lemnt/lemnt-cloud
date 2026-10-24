package com.lemnt.system.domain.auth.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 角色聚合根实体
 *
 * @author wangxx
 */
@Data
@Accessors(chain = true)
public class Role implements Serializable {
    @Serial
    private static final long serialVersionUID = 6198267707181465840L;

    /**
     * 数据范围：自定义数据权限
     */
    public static final String DATA_SCOPE_CUSTOM = "2";

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色权限标识
     */
    private String roleKey;

    /**
     * 角色排序
     */
    private Integer roleSort;

    /**
     * 数据范围
     */
    private String dataScope;



    /**
     * 角色状态（1正常 2停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 用户是否存在此角色标识 默认不存在
     */
    private boolean flag = false;

    /**
     * 菜单组（非DB字段）
     */
    private Long[] menuIds;

    /**
     * 部门组（非DB字段）
     */
    private Long[] deptIds;

    /**
     * 角色菜单权限（非DB字段）
     */
    private Set<String> permissions;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 开始时间（查询用）
     */
    private LocalDateTime beginTime;

    /**
     * 结束时间（查询用）
     */
    private LocalDateTime endTime;

    public boolean isAdmin() {
        return isAdmin(this.roleId);
    }

    public static boolean isAdmin(Long roleId) {
        return roleId != null && 1L == roleId;
    }
}