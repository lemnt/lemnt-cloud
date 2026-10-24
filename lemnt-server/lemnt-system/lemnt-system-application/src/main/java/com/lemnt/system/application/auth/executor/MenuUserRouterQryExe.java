package com.lemnt.system.application.auth.executor;

import com.lemnt.system.application.auth.converter.RouterConverter;
import com.lemnt.system.application.auth.dto.RouterDTO;
import com.lemnt.system.domain.auth.gateway.MenuGateway;
import com.lemnt.system.domain.auth.model.Menu;
import com.lemnt.system.application.auth.service.RouterMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 菜单详情查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class MenuUserRouterQryExe {

    private final MenuGateway menuGateway;
    private final RouterMenuService routerMenuService;

    /**
     * 查询路由信息（按用户）
     */
    public List<RouterDTO> execute(Long userId) {
        List<Menu> menuList;
        if (Objects.equals(userId, 1L)) {
            menuList = menuGateway.selectMenuTreeAll();
        } else {
            menuList = menuGateway.selectMenuTreeByUserId(userId);
        }
        List<Menu> menuTree = routerMenuService.getChildPerms(menuList, 0L);
        return RouterConverter.toDTOList(routerMenuService.buildMenus(menuTree));
    }
}
