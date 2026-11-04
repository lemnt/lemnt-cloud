package com.lemnt.system.infrastructure.auth.gateway;

import com.lemnt.system.domain.auth.gateway.RoleMenuGateway;
import com.lemnt.system.infrastructure.auth.dataobject.RoleMenuDO;
import com.lemnt.system.infrastructure.auth.mapper.RoleMenuMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.List;

/**
 * RoleMenu 持久化实现。
 */
@Component
@RequiredArgsConstructor
public class RoleMenuGatewayImpl implements RoleMenuGateway {
    private final RoleMenuMapper roleMenuMapper;

    @Override
    public Boolean deleteRoleMenuByRoleId(Long roleId) {
        return deleteRoleMenuByRoleIdRow(roleId) > 0;
    }

    @Override
    public Boolean deleteRoleMenu(List<Long> ids) {
        return deleteRoleMenuRow(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertRoleMenus(Long roleId, List<Long> menuIds) {
        if (CollUtil.isEmpty(menuIds)) {
            return true;
        }
        List<RoleMenuDO> list = new ArrayList<>();
        for (Long menuId : menuIds) {
            RoleMenuDO rm = new RoleMenuDO();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            list.add(rm);
        }
        roleMenuMapper.insert(list);
        return true;
    }

    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    private long checkMenuExistRoleRow(Long menuId) {
        return roleMenuMapper.selectCount(
            Wrappers.<RoleMenuDO>lambdaQuery().eq(RoleMenuDO::getMenuId, menuId));
    }

    /**
     * 通过角色ID删除角色菜单关联
     *
     * @param roleId 角色ID
     * @return 结果
     */
    private int deleteRoleMenuByRoleIdRow(Long roleId) {
        return roleMenuMapper.delete(
            Wrappers.<RoleMenuDO>lambdaQuery().eq(RoleMenuDO::getRoleId, roleId));
    }

    /**
     * 批量删除角色菜单关联
     *
     * @param roleIds 角色ID数组
     * @return 结果
     */
    private int deleteRoleMenuRow(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return 0;
        }
        return roleMenuMapper.delete(
            Wrappers.<RoleMenuDO>lambdaQuery().in(RoleMenuDO::getRoleId, roleIds));
    }
}
