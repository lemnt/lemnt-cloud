package com.lemnt.system.application.auth.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.auth.service.TreeMenuService;
import com.lemnt.system.domain.auth.gateway.MenuGateway;
import com.lemnt.system.domain.auth.model.Menu;
import com.lemnt.system.domain.auth.service.RoleDomainService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色菜单树查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class MenuRoleTreeQueryExe {
    private final MenuGateway menuGateway;
    private final TreeMenuService treeMenuService;
    private final RoleDomainService roleDomainService;

    /**
     * 加载对应角色菜单列表树
     *
     * @param roleId 角色ID
     * @return 菜单树和已选中菜单ID列表
     */
    public Map<String, Object> execute(Long roleId) {
        // 读取授权选中项前复用角色详情的数据范围校验。
        roleDomainService.checkRoleDataScope(roleId);
        Long userId = SecurityUtils.getUserId();
        List<Menu> list;
        Menu query = new Menu();
        if (SecurityUtils.isAdmin(userId)) {
            list = menuGateway.selectList(query);
        } else {
            query.setUserId(userId);
            list = menuGateway.selectListByUserId(query);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("checkedKeys", menuGateway.selectMenuListByRoleId(roleId));
        result.put("menus", treeMenuService.buildTreeSelect(list));
        return result;
    }
}
