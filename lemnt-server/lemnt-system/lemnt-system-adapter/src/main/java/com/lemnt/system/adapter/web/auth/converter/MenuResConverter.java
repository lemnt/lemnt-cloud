package com.lemnt.system.adapter.web.auth.converter;

import com.lemnt.system.adapter.web.auth.res.RouterRes;
import com.lemnt.system.adapter.web.auth.res.SystemMenuGetInfoRes;
import com.lemnt.system.adapter.web.auth.res.SystemMenuListRes;
import com.lemnt.system.application.auth.dto.MenuDTO;
import com.lemnt.system.application.auth.dto.RouterDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单响应对象组装器
 *
 * @author wangxx
 */
public class MenuResConverter {
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

    /**
     * MenuDTO List → MenuInfoRes List
     */
    public static List<SystemMenuListRes> toResListToSystemMenuListRes(List<MenuDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemMenuListRes> result = new ArrayList<>(list.size());
        for (MenuDTO item : list) {
            result.add(toResToSystemMenuListRes(item));
        }
        return result;
    }

    /**
     * MenuDTO → MenuInfoRes
     */
    public static SystemMenuListRes toResToSystemMenuListRes(MenuDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemMenuListRes(
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
                toResListToSystemMenuListRes(dto.getChildren())
        );
    }

    /**
     * MenuDTO → MenuInfoRes
     */
    public static SystemMenuGetInfoRes toResToSystemMenuGetInfoRes(MenuDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemMenuGetInfoRes(
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
                toResListToSystemMenuGetInfoRes(dto.getChildren())
        );
    }

    /**
     * MenuDTO List → MenuInfoRes List
     */
    public static List<SystemMenuGetInfoRes> toResListToSystemMenuGetInfoRes(List<MenuDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemMenuGetInfoRes> result = new ArrayList<>(list.size());
        for (MenuDTO item : list) {
            result.add(toResToSystemMenuGetInfoRes(item));
        }
        return result;
    }
}
