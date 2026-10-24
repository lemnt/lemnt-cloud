package com.lemnt.system.application.log.converter;

import com.lemnt.system.application.log.dto.OperLogDTO;
import com.lemnt.system.domain.log.model.OperLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志 Converter — Entity ↔ DTO
 *
 * @author wangxx
 */
public class OperLogConverter {

    /**
     * Entity → DTO
     */
    public static OperLogDTO toDTO(OperLog entity) {
        if (entity == null) {
            return null;
        }
        OperLogDTO dto = new OperLogDTO();
        dto.setOperId(entity.getOperId());
        dto.setTitle(entity.getTitle());
        dto.setBusinessType(entity.getBusinessType());
        dto.setMethod(entity.getMethod());
        dto.setRequestMethod(entity.getRequestMethod());
        dto.setOperatorType(entity.getOperatorType());
        dto.setOperName(entity.getOperName());
        dto.setDeptName(entity.getDeptName());
        dto.setOperUrl(entity.getOperUrl());
        dto.setOperIp(entity.getOperIp());
        dto.setOperLocation(entity.getOperLocation());
        dto.setOperParam(entity.getOperParam());
        dto.setJsonResult(entity.getJsonResult());
        dto.setStatus(entity.getStatus());
        dto.setErrorMsg(entity.getErrorMsg());
        dto.setOperTime(entity.getOperTime());
        dto.setCostTime(entity.getCostTime());
        return dto;
    }

    /**
     * Entity List → DTO List
     */
    public static List<OperLogDTO> toDTOList(List<OperLog> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<OperLogDTO> result = new ArrayList<>(list.size());
        for (OperLog item : list) {
            result.add(toDTO(item));
        }
        return result;
    }
}
