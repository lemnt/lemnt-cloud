package com.lemnt.system.application.auth.converter;

import com.lemnt.system.application.auth.dto.RouterDTO;
import com.lemnt.system.domain.auth.model.Router;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单 Entity ↔ DTO 组装器
 *
 * @author wangxx
 */
public class RouterConverter {

    public static RouterDTO toDTO(Router entity) {
        if (entity == null) {
            return null;
        }
        RouterDTO dto = new RouterDTO();
        dto.setName(entity.getName());
        dto.setPath(entity.getPath());
        dto.setHidden(entity.isHidden());
        dto.setRedirect(entity.getRedirect());
        dto.setComponent(entity.getComponent());
        dto.setQuery(entity.getQuery());
        dto.setAlwaysShow(entity.getAlwaysShow());
        dto.setMeta(MetaConverter.toDTO(entity.getMeta()));
        if (entity.getChildren() != null && !entity.getChildren().isEmpty()) {
            dto.setChildren(toDTOList(entity.getChildren()));
        }
        return dto;
    }

    public static List<RouterDTO> toDTOList(List<Router> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<RouterDTO> result = new ArrayList<>(list.size());
        for (Router item : list) {
            result.add(toDTO(item));
        }
        return result;
    }
}
