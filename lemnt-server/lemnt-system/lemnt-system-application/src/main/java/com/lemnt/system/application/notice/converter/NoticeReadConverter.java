package com.lemnt.system.application.notice.converter;

import com.lemnt.system.application.notice.dto.NoticeReadUserDTO;
import com.lemnt.system.domain.notice.model.NoticeReadUser;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知公告 Converter — Entity ↔ DTO
 *
 * @author wangxx
 */
public class NoticeReadConverter {

    /**
     * Entity → DTO
     */
    public static NoticeReadUserDTO toDTO(NoticeReadUser entity) {
        if (entity == null) {
            return null;
        }
        NoticeReadUserDTO dto = new NoticeReadUserDTO();
        dto.setUserId(entity.getUserId());
        dto.setUserName(entity.getUserName());
        dto.setNickName(entity.getNickName());
        dto.setDeptName(entity.getDeptName());
        dto.setPhonenumber(entity.getPhonenumber());
        dto.setReadTime(entity.getReadTime());
        return dto;
    }

    /**
     * Entity List → DTO List
     */
    public static List<NoticeReadUserDTO> toDTOList(List<NoticeReadUser> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<NoticeReadUserDTO> result = new ArrayList<>(list.size());
        for (NoticeReadUser item : list) {
            result.add(toDTO(item));
        }
        return result;
    }
}
