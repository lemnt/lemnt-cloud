package com.lemnt.system.application.auth.converter;

import java.util.List;
import java.util.stream.Collectors;

import com.lemnt.system.application.auth.dto.RoleDTO;
import com.lemnt.system.domain.auth.model.Role;

/**
 * 角色组装器
 *
 * @author wangxx
 */
public class RoleConverter {
    /**
     * Entity → DTO
     */
    public static RoleDTO toDTO(Role entity) {
        if (entity == null) {
            return null;
        }
        RoleDTO dto = new RoleDTO();
        dto.setRoleId(entity.getRoleId());
        dto.setRoleName(entity.getRoleName());
        dto.setRoleKey(entity.getRoleKey());
        dto.setRoleSort(entity.getRoleSort());
        dto.setDataScope(entity.getDataScope());
        dto.setStatus(entity.getStatus());
        dto.setFlag(entity.isFlag());
        return dto;
    }

    /**
     * Entity List → DTO List
     */
    public static List<RoleDTO> toDTOList(List<Role> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(RoleConverter::toDTO).collect(Collectors.toList());
    }
}
