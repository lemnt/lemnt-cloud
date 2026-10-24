package com.lemnt.system.application.auth.executor;

import com.lemnt.system.application.auth.converter.MenuConverter;
import com.lemnt.system.application.auth.dto.MenuDTO;
import com.lemnt.system.domain.auth.gateway.MenuGateway;
import com.lemnt.system.domain.auth.model.Menu;
import com.lemnt.system.application.auth.service.RouterMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜单详情查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class MenuTreeQryExe {

    private final MenuGateway menuGateway;
    private final RouterMenuService routerMenuService;

    /**
     * 查询菜单树（全部）
     */
    public List<MenuDTO> execute() {
        List<Menu> menuList = menuGateway.selectMenuTreeAll();
        return MenuConverter.toDTOList(routerMenuService.getChildPerms(menuList, 0L));
    }
}
