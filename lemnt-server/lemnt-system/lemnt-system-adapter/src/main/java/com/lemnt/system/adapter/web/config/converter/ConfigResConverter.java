package com.lemnt.system.adapter.web.config.converter;

import com.lemnt.system.adapter.web.config.res.SystemConfigGetConfigKeyRes;
import com.lemnt.system.adapter.web.config.res.SystemConfigGetInfoRes;
import com.lemnt.system.adapter.web.config.res.SystemConfigListRes;
import com.lemnt.system.application.config.dto.ConfigDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 参数配置响应对象组装器
 *
 * @author wangxx
 */
public class ConfigResConverter {
    /**
     * ConfigDTO List → ConfigInfoRes List
     */
    public static List<SystemConfigListRes> toResListToSystemConfigListRes(List<ConfigDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemConfigListRes> result = new ArrayList<>(list.size());
        for (ConfigDTO item : list) {
            result.add(toResToSystemConfigListRes(item));
        }
        return result;
    }

    /**
     * ConfigDTO → ConfigInfoRes
     */
    public static SystemConfigListRes toResToSystemConfigListRes(ConfigDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemConfigListRes(
                dto.getConfigId(),
                dto.getConfigName(),
                dto.getConfigKey(),
                dto.getConfigValue(),
                dto.getConfigType(),
                dto.getRemark(),
                dto.getCreateTime()
        );
    }

    /**
     * ConfigDTO → ConfigInfoRes
     */
    public static SystemConfigGetInfoRes toResToSystemConfigGetInfoRes(ConfigDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemConfigGetInfoRes(
                dto.getConfigId(),
                dto.getConfigName(),
                dto.getConfigKey(),
                dto.getConfigValue(),
                dto.getConfigType(),
                dto.getRemark(),
                dto.getCreateTime()
        );
    }

    /**
     * ConfigDTO → ConfigInfoRes
     */
    public static SystemConfigGetConfigKeyRes toResToSystemConfigGetConfigKeyRes(ConfigDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemConfigGetConfigKeyRes(
                dto.getConfigId(),
                dto.getConfigName(),
                dto.getConfigKey(),
                dto.getConfigValue(),
                dto.getConfigType(),
                dto.getRemark(),
                dto.getCreateTime()
        );
    }
}
