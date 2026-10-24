package com.lemnt.system.infrastructure.auth.converter;

import java.util.ArrayList;
import java.util.List;

import com.lemnt.system.domain.auth.model.Menu;
import com.lemnt.system.infrastructure.auth.dataobject.MenuDO;

/**
 * 菜单 DO ↔ Entity 转换器
 *
 * @author wangxx
 */
public class MenuConverter {

    /**
     * DO → Entity
     */
    public static Menu toEntity(MenuDO src) {
        if (src == null) return null;
        Menu e = new Menu();
        e.setMenuId(src.getMenuId());
        e.setMenuName(src.getMenuName());
        e.setParentId(src.getParentId());
        e.setOrderNum(src.getOrderNum());
        e.setPath(src.getPath());
        e.setComponent(src.getComponent());
        e.setQuery(src.getQuery());
        e.setRouteName(src.getRouteName());
        e.setIsFrame(src.getIsFrame());
        e.setIsCache(src.getIsCache());
        e.setMenuType(src.getMenuType());
        e.setVisible(src.getVisible());
        e.setStatus(src.getStatus());
        e.setPerms(src.getPerms());
        e.setIcon(src.getIcon());
        e.setCreateBy(src.getCreateBy());
        e.setUpdateBy(src.getUpdateBy());
        return e;
    }

    /**
     * Entity → DO
     */
    public static MenuDO toDO(Menu src) {
        if (src == null) return null;
        MenuDO d = new MenuDO();
        d.setMenuId(src.getMenuId());
        d.setMenuName(src.getMenuName());
        d.setParentId(src.getParentId());
        d.setOrderNum(src.getOrderNum());
        d.setPath(src.getPath());
        d.setComponent(src.getComponent());
        d.setQuery(src.getQuery());
        d.setRouteName(src.getRouteName());
        d.setIsFrame(src.getIsFrame());
        d.setIsCache(src.getIsCache());
        d.setMenuType(src.getMenuType());
        d.setVisible(src.getVisible());
        d.setStatus(src.getStatus());
        d.setPerms(src.getPerms());
        d.setIcon(src.getIcon());
        d.setCreateBy(src.getCreateBy());
        d.setUpdateBy(src.getUpdateBy());
        return d;
    }

    /**
     * DO 列表 → Entity 列表
     */
    public static List<Menu> toEntityList(List<MenuDO> list) {
        if (list == null) return new ArrayList<>();
        List<Menu> result = new ArrayList<>(list.size());
        for (MenuDO item : list) result.add(toEntity(item));
        return result;
    }
}
