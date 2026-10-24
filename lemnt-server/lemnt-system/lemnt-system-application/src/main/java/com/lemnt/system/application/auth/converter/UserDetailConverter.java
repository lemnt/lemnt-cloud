package com.lemnt.system.application.auth.converter;

import com.lemnt.system.application.auth.dto.UserDetailDTO;
import com.lemnt.system.domain.auth.model.User;

/**
 * 用户组装器
 *
 * @author wangxx
 */
public class UserDetailConverter {
    /**
     * Entity → DTO
     */
    public static UserDetailDTO.UserDetail toDTO(User entity) {
        if (entity == null) {
            return null;
        }
        UserDetailDTO.UserDetail dto = new UserDetailDTO.UserDetail();
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
        return dto;
    }
}
