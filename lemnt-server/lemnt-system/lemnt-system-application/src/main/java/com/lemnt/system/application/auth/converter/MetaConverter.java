package com.lemnt.system.application.auth.converter;

import com.lemnt.system.application.auth.dto.MetaDTO;
import com.lemnt.system.domain.auth.model.Meta;

/**
 * 路由显示信息 Entity ↔ DTO 组装器
 *
 * @author wangxx
 */
public class MetaConverter {

    public static MetaDTO toDTO(Meta entity) {
        if (entity == null) {
            return null;
        }
        MetaDTO dto = new MetaDTO();
        dto.setTitle(entity.getTitle());
        dto.setIcon(entity.getIcon());
        dto.setNoCache(entity.isNoCache());
        dto.setLink(entity.getLink());
        return dto;
    }
}
