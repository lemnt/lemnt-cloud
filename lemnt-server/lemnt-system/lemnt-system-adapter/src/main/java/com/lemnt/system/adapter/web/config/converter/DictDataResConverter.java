package com.lemnt.system.adapter.web.config.converter;

import com.lemnt.system.adapter.web.config.res.DictDataRes;
import com.lemnt.system.adapter.web.config.res.SystemDictDataDictTypeRes;
import com.lemnt.system.adapter.web.config.res.SystemDictDataGetInfoRes;
import com.lemnt.system.adapter.web.config.res.SystemDictDataListRes;
import com.lemnt.system.application.config.dto.DictDataDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典数据响应对象组装器
 *
 * @author wangxx
 */
public class DictDataResConverter {
    /**
     * DictDataDTO → DictDataInfoRes
     */
    public static DictDataRes toRes(DictDataDTO dto) {
        if (dto == null) {
            return null;
        }
        return new DictDataRes(
                dto.getDictCode(),
                dto.getDictSort(),
                dto.getDictLabel(),
                dto.getDictValue(),
                dto.getDictType(),
                dto.getCssClass(),
                dto.getListClass(),
                dto.getIsDefault(),
                dto.getStatus(),
                dto.getRemark()
        );
    }

    /**
     * DictDataDTO List → DictDataInfoRes List
     */
    public static List<SystemDictDataListRes> toResListToSystemDictDataListRes(List<DictDataDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemDictDataListRes> result = new ArrayList<>(list.size());
        for (DictDataDTO item : list) {
            result.add(toResToSystemDictDataListRes(item));
        }
        return result;
    }

    /**
     * DictDataDTO → DictDataInfoRes
     */
    public static SystemDictDataListRes toResToSystemDictDataListRes(DictDataDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemDictDataListRes(
                dto.getDictCode(),
                dto.getDictSort(),
                dto.getDictLabel(),
                dto.getDictValue(),
                dto.getDictType(),
                dto.getCssClass(),
                dto.getListClass(),
                dto.getIsDefault(),
                dto.getStatus(),
                dto.getRemark()
        );
    }

    /**
     * DictDataDTO → DictDataInfoRes
     */
    public static SystemDictDataGetInfoRes toResToSystemDictDataGetInfoRes(DictDataDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemDictDataGetInfoRes(
                dto.getDictCode(),
                dto.getDictSort(),
                dto.getDictLabel(),
                dto.getDictValue(),
                dto.getDictType(),
                dto.getCssClass(),
                dto.getListClass(),
                dto.getIsDefault(),
                dto.getStatus(),
                dto.getRemark()
        );
    }

    /**
     * DictDataDTO List → DictDataInfoRes List
     */
    public static List<SystemDictDataDictTypeRes> toResListToSystemDictDataDictTypeRes(List<DictDataDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemDictDataDictTypeRes> result = new ArrayList<>(list.size());
        for (DictDataDTO item : list) {
            result.add(toResToSystemDictDataDictTypeRes(item));
        }
        return result;
    }

    /**
     * DictDataDTO → DictDataInfoRes
     */
    public static SystemDictDataDictTypeRes toResToSystemDictDataDictTypeRes(DictDataDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemDictDataDictTypeRes(
                dto.getDictCode(),
                dto.getDictSort(),
                dto.getDictLabel(),
                dto.getDictValue(),
                dto.getDictType(),
                dto.getCssClass(),
                dto.getListClass(),
                dto.getIsDefault(),
                dto.getStatus(),
                dto.getRemark()
        );
    }
}
