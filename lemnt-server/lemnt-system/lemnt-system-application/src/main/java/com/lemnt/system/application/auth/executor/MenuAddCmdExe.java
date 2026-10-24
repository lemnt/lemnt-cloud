package com.lemnt.system.application.auth.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.system.application.auth.cmd.MenuAddCmd;
import com.lemnt.system.domain.auth.model.Menu;
import com.lemnt.system.domain.auth.gateway.MenuGateway;

/**
 * 新增菜单命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class MenuAddCmdExe {

    private final MenuGateway menuGateway;

    /** 新增前校验同级名称，避免其他调用入口绕过业务规则。 */
    public Boolean execute(MenuAddCmd cmd) {
        if (menuGateway.checkMenuNameUnique(cmd.menuName(), cmd.parentId()) != null) {
            throw new ServiceException("新增菜单'" + cmd.menuName() + "'失败，菜单名称已存在");
        }
        Menu entity = new Menu();
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
        return menuGateway.insert(entity);
    }
}
