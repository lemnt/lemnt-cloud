package com.lemnt.system.application.auth.converter;

import com.lemnt.system.application.auth.dto.RoleDTO;
import com.lemnt.system.application.auth.dto.UserDTO;
import com.lemnt.system.application.org.converter.DeptConverter;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.domain.auth.model.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户组装器
 *
 * @author wangxx
 */
public class UserConverter {
    /**
     * Entity → DTO
     */
    public static UserDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setUserId(entity.getUserId());
        dto.setDeptId(entity.getDeptId());
        dto.setUserName(entity.getUserName());
        dto.setNickName(entity.getNickName());
        dto.setEmail(entity.getEmail());
        dto.setPhonenumber(entity.getPhonenumber());
        dto.setSex(entity.getSex());
        dto.setAvatar(entity.getAvatar());
        dto.setStatus(entity.getStatus());
        dto.setLoginIp(entity.getLoginIp());
        dto.setLoginDate(entity.getLoginDate());
        dto.setCreateBy(entity.getCreateBy());
        dto.setRoles(RoleConverter.toDTOList(entity.getRoles()));
        dto.setDept(DeptConverter.toDTO(entity.getDept()));
        return dto;
    }

    /**
     * Entity List → DTO List
     */
    public static List<UserDTO> toDTOList(List<User> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(UserConverter::toDTO).collect(Collectors.toList());
    }

    /**
     * RoleEntity → RoleDTO
     */
    public static RoleDTO toRoleDTO(Role role) {
        if (role == null) {
            return null;
        }
        RoleDTO dto = new RoleDTO();
        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getRoleName());
        dto.setRoleKey(role.getRoleKey());
        dto.setRoleSort(role.getRoleSort());
        dto.setDataScope(role.getDataScope());
        dto.setStatus(role.getStatus());
        dto.setFlag(role.isFlag());
        return dto;
    }
}
