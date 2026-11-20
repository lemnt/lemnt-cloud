package com.lemnt.system.application.config.converter;

import com.lemnt.system.application.config.dto.DictDataDTO;
import com.lemnt.system.domain.config.model.DictData;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典数据 Entity ↔ DTO 组装器
 *
 * @author wangxx
 */
public class DictDataConverter {
    public static DictDataDTO toDTO(DictData entity) {
        if (entity == null) {
            return null;
        }
        DictDataDTO dto = new DictDataDTO();
        dto.setDictCode(entity.getDictCode());
        dto.setDictSort(entity.getDictSort());
        dto.setDictLabel(entity.getDictLabel());
        dto.setDictValue(entity.getDictValue());
        dto.setDictType(entity.getDictType());
        dto.setCssClass(entity.getCssClass());
        dto.setListClass(entity.getListClass());
        dto.setIsDefault(entity.getIsDefault());
        dto.setStatus(entity.getStatus());
        dto.setRemark(entity.getRemark());
        return dto;
    }

    public static List<DictDataDTO> toDTOList(List<DictData> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<DictDataDTO> result = new ArrayList<>(list.size());
        for (DictData item : list) {
            result.add(toDTO(item));
        }
        return result;
    }
}
