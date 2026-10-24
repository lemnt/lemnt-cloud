package com.lemnt.system.infrastructure.log.converter;

import com.lemnt.system.domain.log.model.Logininfor;
import com.lemnt.system.infrastructure.log.dataobject.LogininforDO;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录日志 DO ↔ Entity 转换器
 *
 * @author wangxx
 */
public class LogininforConverter {

    /**
     * DO → Entity
     */
    public static Logininfor toEntity(LogininforDO source) {
        if (source == null) return null;
        Logininfor entity = new Logininfor();
        entity.setInfoId(source.getInfoId());
        entity.setUserName(source.getUserName());
        entity.setIpaddr(source.getIpaddr());
        entity.setLoginLocation(source.getLoginLocation());
        entity.setBrowser(source.getBrowser());
        entity.setOs(source.getOs());
        entity.setStatus(source.getStatus());
        entity.setMsg(source.getMsg());
        entity.setAccessTime(source.getAccessTime());
        return entity;
    }

    /**
     * Entity → DO
     */
    public static LogininforDO toDO(Logininfor source) {
        if (source == null) return null;
        LogininforDO data = new LogininforDO();
        data.setInfoId(source.getInfoId());
        data.setUserName(source.getUserName());
        data.setIpaddr(source.getIpaddr());
        data.setLoginLocation(source.getLoginLocation());
        data.setBrowser(source.getBrowser());
        data.setOs(source.getOs());
        data.setStatus(source.getStatus());
        data.setMsg(source.getMsg());
        data.setAccessTime(source.getAccessTime());
        return data;
    }

    /**
     * DO 列表 → Entity 列表
     */
    public static List<Logininfor> toEntityList(List<LogininforDO> list) {
        if (list == null) return new ArrayList<>();
        List<Logininfor> result = new ArrayList<>(list.size());
        for (LogininforDO item : list) result.add(toEntity(item));
        return result;
    }
}
