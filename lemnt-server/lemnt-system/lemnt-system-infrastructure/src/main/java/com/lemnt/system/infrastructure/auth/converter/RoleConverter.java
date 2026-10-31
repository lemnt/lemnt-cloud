package com.lemnt.system.infrastructure.auth.converter;

import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.infrastructure.auth.dataobject.RoleDO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色对象转换器
 *
 * @author wangxx
 */
public class RoleConverter {
    /**
     * DO → Entity
     */
    public static Role toEntity(RoleDO do0) {
        if (do0 == null) {
            return null;
        }
        Role entity = new Role();
        entity.setRoleId(do0.getRoleId());
        entity.setRoleName(do0.getRoleName());
        entity.setRoleKey(do0.getRoleKey());
        entity.setRoleSort(do0.getRoleSort());
        entity.setDataScope(do0.getDataScope());
        entity.setStatus(do0.getStatus());
        entity.setRemark(do0.getRemark());
        entity.setCreateBy(do0.getCreateBy());
        entity.setUpdateBy(do0.getUpdateBy());
        return entity;
    }

    /**
     * Entity → DO
     */
    public static RoleDO toDO(Role entity) {
        if (entity == null) return null;
        RoleDO item = new RoleDO();
        item.setRoleId(entity.getRoleId());
        item.setRoleName(entity.getRoleName());
        item.setRoleKey(entity.getRoleKey());
        item.setRoleSort(entity.getRoleSort());
        item.setDataScope(entity.getDataScope());
        item.setStatus(entity.getStatus());
        item.setRemark(entity.getRemark());
        item.setCreateBy(entity.getCreateBy());
        item.setUpdateBy(entity.getUpdateBy());
        return item;
    }

    /**
     * DO 列表 → Entity 列表
     */
    public static List<Role> toEntityList(List<RoleDO> doList) {
        if (doList == null) return null;
        return doList.stream().map(RoleConverter::toEntity).collect(Collectors.toList());
    }
}
