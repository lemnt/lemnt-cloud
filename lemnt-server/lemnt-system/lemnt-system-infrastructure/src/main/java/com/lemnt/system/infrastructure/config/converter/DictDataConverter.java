package com.lemnt.system.infrastructure.config.converter;

import com.lemnt.system.domain.config.model.DictData;
import com.lemnt.system.infrastructure.config.dataobject.DictDataDO;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典数据 DO ↔ Entity 转换器
 *
 * @author wangxx
 */
public class DictDataConverter {

    /**
     * DO → Entity
     */
    public static DictData toEntity(DictDataDO src) {
        if (src == null) return null;
        DictData e = new DictData();
        e.setDictCode(src.getDictCode());
        e.setDictSort(src.getDictSort());
        e.setDictLabel(src.getDictLabel());
        e.setDictValue(src.getDictValue());
        e.setDictType(src.getDictType());
        e.setCssClass(src.getCssClass());
        e.setListClass(src.getListClass());
        e.setIsDefault(src.getIsDefault());
        e.setStatus(src.getStatus());
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
    public static DictDataDO toDO(DictData src) {
        if (src == null) return null;
        DictDataDO d = new DictDataDO();
        d.setDictCode(src.getDictCode());
        d.setDictSort(src.getDictSort());
        d.setDictLabel(src.getDictLabel());
        d.setDictValue(src.getDictValue());
        d.setDictType(src.getDictType());
        d.setCssClass(src.getCssClass());
        d.setListClass(src.getListClass());
        d.setIsDefault(src.getIsDefault());
        d.setStatus(src.getStatus());
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
    public static List<DictData> toEntityList(List<DictDataDO> list) {
        if (list == null) return new ArrayList<>();
        List<DictData> result = new ArrayList<>(list.size());
        for (DictDataDO item : list) result.add(toEntity(item));
        return result;
    }
}
