package com.lemnt.system.infrastructure.log.converter;

import com.lemnt.system.domain.log.model.OperLog;
import com.lemnt.system.infrastructure.log.dataobject.OperLogDO;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志 DO ↔ Entity 转换器
 *
 * @author wangxx
 */
public class OperLogConverter {

    /**
     * DO → Entity
     */
    public static OperLog toEntity(OperLogDO source) {
        if (source == null) return null;
        OperLog entity = new OperLog();
        entity.setOperId(source.getOperId());
        entity.setTitle(source.getTitle());
        entity.setBusinessType(source.getBusinessType());
        entity.setMethod(source.getMethod());
        entity.setRequestMethod(source.getRequestMethod());
        entity.setOperatorType(source.getOperatorType());
        entity.setOperName(source.getOperName());
        entity.setDeptName(source.getDeptName());
        entity.setOperUrl(source.getOperUrl());
        entity.setOperIp(source.getOperIp());
        entity.setOperLocation(source.getOperLocation());
        entity.setOperParam(source.getOperParam());
        entity.setJsonResult(source.getJsonResult());
        entity.setStatus(source.getStatus());
        entity.setErrorMsg(source.getErrorMsg());
        entity.setOperTime(source.getOperTime());
        entity.setCostTime(source.getCostTime());
        return entity;
    }

    /**
     * Entity → DO
     */
    public static OperLogDO toDO(OperLog source) {
        if (source == null) return null;
        OperLogDO data = new OperLogDO();
        data.setOperId(source.getOperId());
        data.setTitle(source.getTitle());
        data.setBusinessType(source.getBusinessType());
        data.setMethod(source.getMethod());
        data.setRequestMethod(source.getRequestMethod());
        data.setOperatorType(source.getOperatorType());
        data.setOperName(source.getOperName());
        data.setDeptName(source.getDeptName());
        data.setOperUrl(source.getOperUrl());
        data.setOperIp(source.getOperIp());
        data.setOperLocation(source.getOperLocation());
        data.setOperParam(source.getOperParam());
        data.setJsonResult(source.getJsonResult());
        data.setStatus(source.getStatus());
        data.setErrorMsg(source.getErrorMsg());
        data.setOperTime(source.getOperTime());
        data.setCostTime(source.getCostTime());
        return data;
    }

    /**
     * DO 列表 → Entity 列表
     */
    public static List<OperLog> toEntityList(List<OperLogDO> list) {
        if (list == null) return new ArrayList<>();
        List<OperLog> result = new ArrayList<>(list.size());
        for (OperLogDO item : list) result.add(toEntity(item));
        return result;
    }
}
