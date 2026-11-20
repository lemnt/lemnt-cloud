package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.common.datascope.annotation.DataScope;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.infrastructure.auth.dataobject.RoleDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Role 数据层
 *
 * @author wangxx
 */
@Mapper
public interface RoleMapper extends BaseMapper<RoleDO> {
    /**
     * 根据条件分页查询角色列表
     *
     * @param page 分页参数
     * @param role 角色信息
     * @return 分页结果
     */
    @DataScope(deptAlias = "d")
    IPage<RoleDO> selectPage(Page<RoleDO> page, @Param("qry") Role role);

    /**
     * 根据条件查询角色列表
     *
     * @param role
     * @return
     */
    @DataScope(deptAlias = "d")
    List<RoleDO> selectRoleList(@Param("qry") Role role);

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<RoleDO> selectRolePermissionByUserId(Long userId);

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    @DataScope(deptAlias = "d")
    List<RoleDO> selectRoleAll();

    /**
     * 根据用户ID获取角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleListByUserId(Long userId);

    /**
     * 根据用户名查询角色
     *
     * @param userName 用户名
     * @return 角色列表
     */
    List<RoleDO> selectRolesByUserName(String userName);

    /**
     * 校验角色名称是否唯一
     *
     * @param roleName 角色名称
     * @return 角色信息
     */
    RoleDO checkRoleNameUnique(String roleName);

    /**
     * 校验角色权限是否唯一
     *
     * @param roleKey 角色权限
     * @return 角色信息
     */
    RoleDO checkRoleKeyUnique(String roleKey);
}
