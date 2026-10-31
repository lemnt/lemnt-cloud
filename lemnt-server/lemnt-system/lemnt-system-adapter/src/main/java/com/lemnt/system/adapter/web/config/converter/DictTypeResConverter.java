package com.lemnt.system.adapter.web.config.converter;

import com.lemnt.system.adapter.web.config.res.SystemDictTypeGetInfoRes;
import com.lemnt.system.adapter.web.config.res.SystemDictTypeListRes;
import com.lemnt.system.adapter.web.config.res.SystemDictTypeOptionSelectRes;
import com.lemnt.system.application.config.dto.DictTypeDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典类型响应对象组装器
 *
 * @author wangxx
 */
public class DictTypeResConverter {
    /**
     * DictTypeDTO List → DictTypeInfoRes List
     */
    public static List<SystemDictTypeListRes> toResListToSystemDictTypeListRes(List<DictTypeDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemDictTypeListRes> result = new ArrayList<>(list.size());
        for (DictTypeDTO item : list) {
            result.add(toResToSystemDictTypeListRes(item));
        }
        return result;
    }

    /**
     * DictTypeDTO → DictTypeInfoRes
     */
    public static SystemDictTypeListRes toResToSystemDictTypeListRes(DictTypeDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemDictTypeListRes(
                dto.getDictId(),
                dto.getDictName(),
                dto.getDictType(),
                dto.getStatus(),
                dto.getRemark()
        );
    }

    /**
     * DictTypeDTO → DictTypeInfoRes
     */
    public static SystemDictTypeGetInfoRes toResToSystemDictTypeGetInfoRes(DictTypeDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemDictTypeGetInfoRes(
                dto.getDictId(),
                dto.getDictName(),
                dto.getDictType(),
                dto.getStatus(),
                dto.getRemark()
        );
    }

    /**
     * DictTypeDTO List → DictTypeInfoRes List
     */
    public static List<SystemDictTypeOptionSelectRes> toResListToSystemDictTypeOptionSelectRes(List<DictTypeDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemDictTypeOptionSelectRes> result = new ArrayList<>(list.size());
        for (DictTypeDTO item : list) {
            result.add(toResToSystemDictTypeOptionSelectRes(item));
        }
        return result;
    }

    /**
     * DictTypeDTO → DictTypeInfoRes
     */
    public static SystemDictTypeOptionSelectRes toResToSystemDictTypeOptionSelectRes(DictTypeDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemDictTypeOptionSelectRes(
                dto.getDictId(),
                dto.getDictName(),
                dto.getDictType(),
                dto.getStatus(),
                dto.getRemark()
        );
    }
}
