package com.lemnt.system.adapter.web.auth.converter;

import java.util.ArrayList;
import java.util.List;

import com.lemnt.system.adapter.web.auth.res.MenuRes;
import com.lemnt.system.adapter.web.auth.res.RouterRes;
import com.lemnt.system.application.auth.dto.MenuDTO;
import com.lemnt.system.application.auth.dto.RouterDTO;

/**
 * 菜单响应对象组装器
 *
 * @author wangxx
 */
public class MenuResConverter {

    /**
     * MenuDTO → MenuInfoRes
     */
    public static MenuRes toRes(MenuDTO dto) {
        if (dto == null) {
            return null;
        }
        return new MenuRes(
                dto.getMenuId(),
                dto.getMenuName(),
                dto.getParentId(),
                dto.getOrderNum(),
                dto.getPath(),
                dto.getComponent(),
                dto.getQuery(),
                dto.getRouteName(),
                dto.getIsFrame(),
                dto.getIsCache(),
                dto.getMenuType(),
                dto.getVisible(),
                dto.getStatus(),
                dto.getPerms(),
                dto.getIcon(),
                toResList(dto.getChildren())
        );
    }

    /**
     * MenuDTO List → MenuInfoRes List
     */
    public static List<MenuRes> toResList(List<MenuDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<MenuRes> result = new ArrayList<>(list.size());
        for (MenuDTO item : list) {
            result.add(toRes(item));
        }
        return result;
    }

    /**
     * RouterDTO → RouterInfoRes
     */
    public static RouterRes toRouterInfoRes(RouterDTO dto) {
        if (dto == null) {
            return null;
        }
        return new RouterRes(
                dto.getName(),
                dto.getPath(),
                dto.isHidden(),
                dto.getRedirect(),
                dto.getComponent(),
                dto.getQuery(),
                dto.getAlwaysShow(),
                dto.getMeta(),
                toRouterResList(dto.getChildren())
        );
    }

    /**
     * RouterDTO List → RouterInfoRes List
     */
    public static List<RouterRes> toRouterResList(List<RouterDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<RouterRes> result = new ArrayList<>(list.size());
        for (RouterDTO item : list) {
            result.add(toRouterInfoRes(item));
        }
        return result;
    }
}
