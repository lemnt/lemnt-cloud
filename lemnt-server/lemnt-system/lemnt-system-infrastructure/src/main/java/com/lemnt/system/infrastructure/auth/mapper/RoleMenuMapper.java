package com.lemnt.system.infrastructure.auth.mapper;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.system.infrastructure.auth.dataobject.RoleMenuDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * RoleRelation 数据层
 *
 * @author wangxx
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenuDO> {

    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    default long checkMenuExistRole(Long menuId) {
        return this.selectCount(Wrappers.<RoleMenuDO>lambdaQuery()
                .eq(RoleMenuDO::getMenuId, menuId));
    }

    /**
     * 通过角色ID删除角色菜单关联
     *
     * @param roleId 角色ID
     * @return 结果
     */
    default int deleteRoleMenuByRoleId(Long roleId) {
        return this.delete(Wrappers.<RoleMenuDO>lambdaQuery()
                .eq(RoleMenuDO::getRoleId, roleId));
    }

    /**
     * 批量删除角色菜单关联
     *
     * @param roleIds 角色ID数组
     * @return 结果
     */
    default int deleteRoleMenu(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return 0;
        }
        return this.delete(Wrappers.<RoleMenuDO>lambdaQuery()
                .in(RoleMenuDO::getRoleId, roleIds));
    }
}
