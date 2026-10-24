package com.lemnt.system.application.log.converter;

import com.lemnt.system.application.log.dto.LogininforDTO;
import com.lemnt.system.domain.log.model.Logininfor;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录日志 Converter — Entity ↔ DTO
 *
 * @author wangxx
 */
public class LogininforConverter {

    /**
     * Entity → DTO
     */
    public static LogininforDTO toDTO(Logininfor entity) {
        if (entity == null) {
            return null;
        }
        LogininforDTO dto = new LogininforDTO();
        dto.setInfoId(entity.getInfoId());
        dto.setUserName(entity.getUserName());
        dto.setIpaddr(entity.getIpaddr());
        dto.setStatus(entity.getStatus());
        dto.setMsg(entity.getMsg());
        dto.setAccessTime(entity.getAccessTime());
        return dto;
    }

    /**
     * Entity List → DTO List
     */
    public static List<LogininforDTO> toDTOList(List<Logininfor> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<LogininforDTO> result = new ArrayList<>(list.size());
        for (Logininfor item : list) {
            result.add(toDTO(item));
        }
        return result;
    }
}
