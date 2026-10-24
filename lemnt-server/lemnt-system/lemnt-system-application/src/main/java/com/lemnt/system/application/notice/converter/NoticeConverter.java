package com.lemnt.system.application.notice.converter;

import com.lemnt.system.application.notice.dto.NoticeDTO;
import com.lemnt.system.domain.notice.model.Notice;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知公告 Converter — Entity ↔ DTO
 *
 * @author wangxx
 */
public class NoticeConverter {
    /**
     * Entity → DTO
     */
    public static NoticeDTO toDTO(Notice entity) {
        return toDTO(entity, entity != null && Boolean.TRUE.equals(entity.getIsRead()));
    }

    /**
     * Entity → DTO（携带已读状态）
     */
    public static NoticeDTO toDTO(Notice entity, boolean isRead) {
        if (entity == null) {
            return null;
        }
        NoticeDTO dto = new NoticeDTO();
        dto.setNoticeId(entity.getNoticeId());
        dto.setNoticeTitle(entity.getNoticeTitle());
        dto.setNoticeType(entity.getNoticeType());
        dto.setNoticeContent(entity.getNoticeContent());
        dto.setStatus(entity.getStatus());
        dto.setIsRead(isRead);
        dto.setCreateBy(entity.getCreateBy());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateBy(entity.getUpdateBy());
        dto.setUpdateTime(entity.getUpdateTime());
        dto.setRemark(entity.getRemark());
        return dto;
    }

    /**
     * Entity List → DTO List
     */
    public static List<NoticeDTO> toDTOList(List<Notice> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<NoticeDTO> result = new ArrayList<>(list.size());
        for (Notice item : list) {
            result.add(toDTO(item));
        }
        return result;
    }
}
