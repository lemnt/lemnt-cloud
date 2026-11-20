package com.lemnt.system.infrastructure.org.converter;

import com.lemnt.system.domain.org.model.Dept;
import com.lemnt.system.infrastructure.org.dataobject.DeptDO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门对象转换器
 *
 * @author wangxx
 */
public class DeptConverter {
    /**
     * DO → Entity
     */
    public static Dept toEntity(DeptDO do0) {
        if (do0 == null) return null;
        Dept entity = new Dept();
        entity.setDeptId(do0.getDeptId());
        entity.setParentId(do0.getParentId());
        entity.setAncestors(do0.getAncestors());
        entity.setDeptName(do0.getDeptName());
        entity.setOrderNum(do0.getOrderNum());
        entity.setLeader(do0.getLeader());
        entity.setPhone(do0.getPhone());
        entity.setEmail(do0.getEmail());
        entity.setStatus(do0.getStatus());
        entity.setDelFlag(do0.getDelFlag());
        entity.setCreateBy(do0.getCreateBy());
        entity.setCreateTime(do0.getCreateTime());
        entity.setUpdateBy(do0.getUpdateBy());
        return entity;
    }

    /**
     * Entity → DO
     */
    public static DeptDO toDO(Dept entity) {
        if (entity == null) return null;
        DeptDO do0 = new DeptDO();
        do0.setDeptId(entity.getDeptId());
        do0.setParentId(entity.getParentId());
        do0.setAncestors(entity.getAncestors());
        do0.setDeptName(entity.getDeptName());
        do0.setOrderNum(entity.getOrderNum());
        do0.setLeader(entity.getLeader());
        do0.setPhone(entity.getPhone());
        do0.setEmail(entity.getEmail());
        do0.setStatus(entity.getStatus());
        do0.setDelFlag(entity.getDelFlag());
        do0.setCreateBy(entity.getCreateBy());
        do0.setUpdateBy(entity.getUpdateBy());
        return do0;
    }

    /**
     * DO 列表 → Entity 列表
     */
    public static List<Dept> toEntityList(List<DeptDO> doList) {
        if (doList == null) return null;
        return doList.stream().map(DeptConverter::toEntity).collect(Collectors.toList());
    }

    /**
     * Entity 列表 → DO 列表
     */
    public static List<DeptDO> toDOList(List<Dept> entityList) {
        if (entityList == null) return null;
        return entityList.stream().map(DeptConverter::toDO).collect(Collectors.toList());
    }
}
