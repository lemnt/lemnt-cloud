package com.lemnt.system.application.org.converter;

import java.util.List;
import java.util.stream.Collectors;

import com.lemnt.system.application.org.dto.DeptDTO;
import com.lemnt.system.domain.org.model.Dept;

/**
 * 部门组装器
 *
 * @author wangxx
 */
public class DeptConverter {
    /**
     * Entity → DTO
     */
    public static DeptDTO toDTO(Dept entity) {
        if (entity == null) {
            return null;
        }
        DeptDTO dto = new DeptDTO();
        dto.setDeptId(entity.getDeptId());
        dto.setParentId(entity.getParentId());
        dto.setAncestors(entity.getAncestors());
        dto.setDeptName(entity.getDeptName());
        dto.setOrderNum(entity.getOrderNum());
        dto.setLeader(entity.getLeader());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setStatus(entity.getStatus());
        dto.setParentName(entity.getParentName());
        dto.setCreateBy(entity.getCreateBy());
        dto.setCreateTime(entity.getCreateTime());
        if (entity.getChildren() != null) {
            dto.setChildren(entity.getChildren().stream().map(DeptConverter::toDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    /**
     * DTO List
     */
    public static List<DeptDTO> toDTOList(List<Dept> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(DeptConverter::toDTO).collect(Collectors.toList());
    }
}
