package com.lemnt.system.api.domain.dto;

import com.lemnt.common.api.domain.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户对象 sys_user
 *
 * @author wangxx
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class UserInfoRoleDTO extends BaseDTO {

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
