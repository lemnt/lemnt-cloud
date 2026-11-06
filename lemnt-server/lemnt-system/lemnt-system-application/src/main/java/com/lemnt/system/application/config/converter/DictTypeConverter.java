package com.lemnt.system.application.config.converter;

import com.lemnt.system.application.config.dto.DictTypeDTO;
import com.lemnt.system.domain.config.model.DictType;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典类型 Entity ↔ DTO 组装器
 *
 * @author wangxx
 */
public class DictTypeConverter {
    public static DictTypeDTO toDTO(DictType entity) {
        if (entity == null) {
            return null;
        }
        DictTypeDTO dto = new DictTypeDTO();
        dto.setDictId(entity.getDictId());
        dto.setDictName(entity.getDictName());
        dto.setDictType(entity.getDictType());
        dto.setStatus(entity.getStatus());
        dto.setRemark(entity.getRemark());
        return dto;
    }

    public static List<DictTypeDTO> toDTOList(List<DictType> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<DictTypeDTO> result = new ArrayList<>(list.size());
        for (DictType item : list) {
            result.add(toDTO(item));
        }
        return result;
    }
}
