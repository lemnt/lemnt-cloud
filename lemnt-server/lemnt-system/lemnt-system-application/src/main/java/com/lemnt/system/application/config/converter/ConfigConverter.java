package com.lemnt.system.application.config.converter;

import java.util.ArrayList;
import java.util.List;

import com.lemnt.system.application.config.dto.ConfigDTO;
import com.lemnt.system.domain.config.model.Config;

/**
 * 参数配置 Entity ↔ DTO 组装器
 *
 * @author wangxx
 */
public class ConfigConverter {

    public static ConfigDTO toDTO(Config entity) {
        if (entity == null) {
            return null;
        }
        ConfigDTO dto = new ConfigDTO();
        dto.setConfigId(entity.getConfigId());
        dto.setConfigName(entity.getConfigName());
        dto.setConfigKey(entity.getConfigKey());
        dto.setConfigValue(entity.getConfigValue());
        dto.setConfigType(entity.getConfigType());
        dto.setRemark(entity.getRemark());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }

    public static List<ConfigDTO> toDTOList(List<Config> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<ConfigDTO> result = new ArrayList<>(list.size());
        for (Config item : list) {
            result.add(toDTO(item));
        }
        return result;
    }
}
