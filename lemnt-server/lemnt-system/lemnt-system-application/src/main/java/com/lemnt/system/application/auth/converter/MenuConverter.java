package com.lemnt.system.application.auth.converter;

import com.lemnt.system.application.auth.dto.MenuDTO;
import com.lemnt.system.domain.auth.model.Menu;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单 Entity ↔ DTO 组装器
 *
 * @author wangxx
 */
public class MenuConverter {
    public static MenuDTO toDTO(Menu entity) {
        if (entity == null) return null;
        MenuDTO dto = new MenuDTO();
        dto.setMenuId(entity.getMenuId());
        dto.setMenuName(entity.getMenuName());
        dto.setParentId(entity.getParentId());
        dto.setOrderNum(entity.getOrderNum());
        dto.setPath(entity.getPath());
        dto.setComponent(entity.getComponent());
        dto.setQuery(entity.getQuery());
        dto.setRouteName(entity.getRouteName());
        dto.setIsFrame(entity.getIsFrame());
        dto.setIsCache(entity.getIsCache());
        dto.setMenuType(entity.getMenuType());
        dto.setVisible(entity.getVisible());
        dto.setStatus(entity.getStatus());
        dto.setPerms(entity.getPerms());
        dto.setIcon(entity.getIcon());
        if (entity.getChildren() != null && !entity.getChildren().isEmpty()) {
            dto.setChildren(toDTOList(entity.getChildren()));
        }
        return dto;
    }

    public static List<MenuDTO> toDTOList(List<Menu> list) {
        if (list == null) return new ArrayList<>();
        List<MenuDTO> result = new ArrayList<>(list.size());
        for (Menu item : list) result.add(toDTO(item));
        return result;
    }
}
