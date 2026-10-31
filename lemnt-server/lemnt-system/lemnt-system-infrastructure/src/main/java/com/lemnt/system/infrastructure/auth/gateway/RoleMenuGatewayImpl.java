package com.lemnt.system.infrastructure.auth.gateway;

import com.lemnt.system.domain.auth.gateway.RoleMenuGateway;
import com.lemnt.system.infrastructure.auth.dataobject.RoleMenuDO;
import com.lemnt.system.infrastructure.auth.mapper.RoleMenuMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.util.*;

/** RoleMenu 持久化实现。 */
@Component
@RequiredArgsConstructor
public class RoleMenuGatewayImpl implements RoleMenuGateway {
    private final RoleMenuMapper roleMenuMapper;

    @Override
    public Boolean deleteRoleMenuByRoleId(Long roleId) {
        return roleMenuMapper.deleteRoleMenuByRoleId(roleId) > 0;
    }

    @Override
    public Boolean deleteRoleMenu(List<Long> ids) {
        return roleMenuMapper.deleteRoleMenu(ids) > 0;
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
}
