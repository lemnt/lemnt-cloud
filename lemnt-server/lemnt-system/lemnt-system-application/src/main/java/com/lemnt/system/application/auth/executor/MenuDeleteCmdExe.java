package com.lemnt.system.application.auth.executor;

import com.lemnt.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.auth.gateway.MenuGateway;

/**
 * 删除菜单命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class MenuDeleteCmdExe {

    private final MenuGateway menuGateway;

    public Boolean execute(Long menuId) {
        if (menuGateway.hasChildByMenuId(menuId)) {
            throw new ServiceException("存在子菜单,不允许删除");
        }
        return menuGateway.deleteById(menuId);
    }
}
