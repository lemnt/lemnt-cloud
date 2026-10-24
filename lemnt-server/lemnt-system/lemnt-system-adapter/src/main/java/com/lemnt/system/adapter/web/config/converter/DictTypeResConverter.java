package com.lemnt.system.adapter.web.config.converter;

import java.util.ArrayList;
import java.util.List;

import com.lemnt.system.adapter.web.config.res.DictTypeRes;
import com.lemnt.system.application.config.dto.DictTypeDTO;

/**
 * 字典类型响应对象组装器
 *
 * @author wangxx
 */
public class DictTypeResConverter {

    /**
     * DictTypeDTO → DictTypeInfoRes
     */
    public static DictTypeRes toRes(DictTypeDTO dto) {
        if (dto == null) {
            return null;
        }
        return new DictTypeRes(
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
    public static List<DictTypeRes> toResList(List<DictTypeDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<DictTypeRes> result = new ArrayList<>(list.size());
        for (DictTypeDTO item : list) {
            result.add(toRes(item));
        }
        return result;
    }
}
