package com.lemnt.system.infrastructure.config.converter;

import com.lemnt.system.domain.config.model.DictType;
import com.lemnt.system.infrastructure.config.dataobject.DictTypeDO;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典类型 DO ↔ Entity 转换器
 *
 * @author wangxx
 */
public class DictTypeConverter {

    /**
     * DO → Entity
     */
    public static DictType toEntity(DictTypeDO src) {
        if (src == null) return null;
        DictType e = new DictType();
        e.setDictId(src.getDictId());
        e.setDictName(src.getDictName());
        e.setDictType(src.getDictType());
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
    public static DictTypeDO toDO(DictType src) {
        if (src == null) return null;
        DictTypeDO d = new DictTypeDO();
        d.setDictId(src.getDictId());
        d.setDictName(src.getDictName());
        d.setDictType(src.getDictType());
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
    public static List<DictType> toEntityList(List<DictTypeDO> list) {
        if (list == null) return new ArrayList<>();
        List<DictType> result = new ArrayList<>(list.size());
        for (DictTypeDO item : list) result.add(toEntity(item));
        return result;
    }
}
