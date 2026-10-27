package com.lemnt.system.infrastructure.auth.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.infrastructure.auth.converter.RoleConverter;
import com.lemnt.system.infrastructure.auth.dataobject.RoleDO;
import com.lemnt.system.infrastructure.auth.mapper.RoleMapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * 角色管理 Gateway 实现
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleGatewayImpl implements RoleGateway {
    private final RoleMapper roleMapper;

    @Override
    public PageResult<Role> selectPage(Role role, PageQry pageQry) {
        Page<RoleDO> page = new Page<>(pageQry.getPageNum(), pageQry.getPageSize());
        IPage<RoleDO> result = roleMapper.selectPage(page, role);
        return PageResult.of(RoleConverter.toEntityList(result.getRecords()), result.getTotal());
    }

    @Override
    public List<Role> selectRoleList(Role role) {
        return RoleConverter.toEntityList(roleMapper.selectRoleList(role));
    }

    @Override
    public List<Role> selectRolesByUserId(Long userId) {
        List<RoleDO> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        Set<Long> userRoleIds = new HashSet<>();
        for (RoleDO userRole : userRoles) {
            userRoleIds.add(userRole.getRoleId());
        }
        List<Role> roles = RoleConverter.toEntityList(roleMapper.selectRoleAll());
        for (Role role : roles) {
            if (userRoleIds.contains(role.getRoleId())) {
                role.setFlag(true);
            }
        }
        return roles;
    }

    @Override
    public Set<String> selectRolePermissionByUserId(Long userId) {
        List<RoleDO> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (RoleDO perm : userRoles) {
            if (Objects.nonNull(perm)) {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    @Override
    public List<Role> selectRoleAll() {
        return RoleConverter.toEntityList(roleMapper.selectRoleAll());
    }

    @Override
    public List<Long> selectRoleListByUserId(Long userId) {
        return roleMapper.selectRoleListByUserId(userId);
    }

    @Override
    public Role selectRoleById(Long roleId) {
        return RoleConverter.toEntity(roleMapper.selectById(roleId));
    }

    @Override
    public List<Role> selectRolesByUserName(String userName) {
        return RoleConverter.toEntityList(roleMapper.selectRolesByUserName(userName));
    }

    @Override
    public Role checkRoleNameUnique(String roleName) {
        return RoleConverter.toEntity(roleMapper.checkRoleNameUnique(roleName));
    }

    @Override
    public Role checkRoleKeyUnique(String roleKey) {
        return RoleConverter.toEntity(roleMapper.checkRoleKeyUnique(roleKey));
    }

    @Override
    public Boolean updateRole(Role role) {
        return roleMapper.updateById(RoleConverter.toDO(role)) > 0;
    }

    @Override
    public Boolean insertRole(Role role) {
        RoleDO roleDO = RoleConverter.toDO(role);
        int rows = roleMapper.insert(roleDO);

        // 回填自增主键到 Entity，供调用方获取 roleId
        if (rows > 0) {
            role.setRoleId(roleDO.getRoleId());
        }
        return rows > 0;
    }

    @Override
    public Boolean deleteRoleById(Long roleId) {
        return roleMapper.deleteById(roleId) > 0;
    }

    @Override
    public Boolean deleteRoleByIds(List<Long> roleIds) {
        return roleMapper.deleteByIds(roleIds) > 0;
    }

}
