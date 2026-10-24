package com.lemnt.system.infrastructure.config.converter;

import com.lemnt.system.domain.config.model.Config;
import com.lemnt.system.infrastructure.config.dataobject.ConfigDO;

import java.util.ArrayList;
import java.util.List;

/**
 * 参数配置 DO ↔ Entity 转换器
 *
 * @author wangxx
 */
public class ConfigConverter {

    /**
     * DO → Entity
     */
    public static Config toEntity(ConfigDO src) {
        if (src == null) return null;
        Config e = new Config();
        e.setConfigId(src.getConfigId());
        e.setConfigName(src.getConfigName());
        e.setConfigKey(src.getConfigKey());
        e.setConfigValue(src.getConfigValue());
        e.setConfigType(src.getConfigType());
        e.setRemark(src.getRemark());
        e.setCreateBy(src.getCreateBy());
        e.setCreateTime(src.getCreateTime());
        e.setUpdateBy(src.getUpdateBy());
        e.setUpdateTime(src.getUpdateTime());
        return e;
    }

    /**
     * Entity → DO
     */
    public static ConfigDO toDO(Config src) {
        if (src == null) return null;
        ConfigDO d = new ConfigDO();
        d.setConfigId(src.getConfigId());
        d.setConfigName(src.getConfigName());
        d.setConfigKey(src.getConfigKey());
        d.setConfigValue(src.getConfigValue());
        d.setConfigType(src.getConfigType());
        d.setRemark(src.getRemark());
        d.setCreateBy(src.getCreateBy());
        d.setCreateTime(src.getCreateTime());
        d.setUpdateBy(src.getUpdateBy());
        d.setUpdateTime(src.getUpdateTime());
        return d;
    }

    /**
     * DO 列表 → Entity 列表
     */
    public static List<Config> toEntityList(List<ConfigDO> list) {
        if (list == null) return new ArrayList<>();
        List<Config> result = new ArrayList<>(list.size());
        for (ConfigDO item : list) result.add(toEntity(item));
        return result;
    }
}
