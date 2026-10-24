package com.lemnt.system.adapter.web.config.converter;

import java.util.ArrayList;
import java.util.List;

import com.lemnt.system.adapter.web.config.res.DictDataRes;
import com.lemnt.system.application.config.dto.DictDataDTO;

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
    public static List<DictDataRes> toResList(List<DictDataDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<DictDataRes> result = new ArrayList<>(list.size());
        for (DictDataDTO item : list) {
            result.add(toRes(item));
        }
        return result;
    }
}
