package com.lemnt.system.adapter.web.config.converter;

import java.util.ArrayList;
import java.util.List;

import com.lemnt.system.adapter.web.config.res.ConfigRes;
import com.lemnt.system.application.config.dto.ConfigDTO;

/**
 * 参数配置响应对象组装器
 *
 * @author wangxx
 */
public class ConfigResConverter {

    /**
     * ConfigDTO → ConfigInfoRes
     */
    public static ConfigRes toRes(ConfigDTO dto) {
        if (dto == null) {
            return null;
        }
        return new ConfigRes(
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
     * ConfigDTO List → ConfigInfoRes List
     */
    public static List<ConfigRes> toResList(List<ConfigDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<ConfigRes> result = new ArrayList<>(list.size());
        for (ConfigDTO item : list) {
            result.add(toRes(item));
        }
        return result;
    }
}
