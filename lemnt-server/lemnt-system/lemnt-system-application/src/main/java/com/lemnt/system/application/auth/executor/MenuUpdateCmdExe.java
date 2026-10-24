package com.lemnt.system.application.auth.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.system.application.auth.cmd.MenuUpdateCmd;
import com.lemnt.system.domain.auth.model.Menu;
import com.lemnt.system.domain.auth.gateway.MenuGateway;

/**
 * 修改菜单命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class MenuUpdateCmdExe {

    private final MenuGateway menuGateway;

    /** 修改前校验目标父菜单下的名称，查询排除当前菜单。 */
    public Boolean execute(MenuUpdateCmd cmd) {
        if (menuGateway.checkMenuNameUnique(cmd.menuName(), cmd.parentId(), cmd.menuId()) != null) {
            throw new ServiceException("修改菜单'" + cmd.menuName() + "'失败，菜单名称已存在");
        }
        Menu entity = new Menu();
        entity.setMenuId(cmd.menuId());
        entity.setMenuName(cmd.menuName());
        entity.setParentId(cmd.parentId());
        entity.setOrderNum(cmd.orderNum());
        entity.setPath(cmd.path());
        entity.setComponent(cmd.component());
        entity.setQuery(cmd.query());
        entity.setRouteName(cmd.routeName());
        entity.setIsFrame(cmd.isFrame());
        entity.setIsCache(cmd.isCache());
        entity.setMenuType(cmd.menuType());
        entity.setVisible(cmd.visible());
        entity.setStatus(cmd.status());
        entity.setPerms(cmd.perms());
        entity.setIcon(cmd.icon());
        return menuGateway.update(entity);
    }
}
