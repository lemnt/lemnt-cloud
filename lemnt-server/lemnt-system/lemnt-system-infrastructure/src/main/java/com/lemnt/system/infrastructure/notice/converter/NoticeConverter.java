package com.lemnt.system.infrastructure.notice.converter;

import com.lemnt.system.domain.notice.model.Notice;
import com.lemnt.system.infrastructure.notice.dataobject.NoticeDO;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知公告 DO ↔ Entity 转换器
 *
 * @author wangxx
 */
public class NoticeConverter {

    /**
     * DO → Entity
     */
    public static Notice toEntity(NoticeDO source) {
        if (source == null) return null;
        Notice entity = new Notice();
        entity.setNoticeId(source.getNoticeId());
        entity.setNoticeTitle(source.getNoticeTitle());
        entity.setNoticeType(source.getNoticeType());
        entity.setNoticeContent(source.getNoticeContent());
        entity.setStatus(source.getStatus());
        entity.setRemark(source.getRemark());
        entity.setCreateBy(source.getCreateBy());
        entity.setCreateTime(source.getCreateTime());
        entity.setUpdateBy(source.getUpdateBy());
        entity.setUpdateTime(source.getUpdateTime());
        return entity;
    }

    /**
     * Entity → DO
     */
    public static NoticeDO toDO(Notice source) {
        if (source == null) return null;
        NoticeDO data = new NoticeDO();
        data.setNoticeId(source.getNoticeId());
        data.setNoticeTitle(source.getNoticeTitle());
        data.setNoticeType(source.getNoticeType());
        data.setNoticeContent(source.getNoticeContent());
        data.setStatus(source.getStatus());
        data.setRemark(source.getRemark());
        data.setCreateBy(source.getCreateBy());
        data.setCreateTime(source.getCreateTime());
        data.setUpdateBy(source.getUpdateBy());
        data.setUpdateTime(source.getUpdateTime());
        return data;
    }

    /**
     * DO 列表 → Entity 列表
     */
    public static List<Notice> toEntityList(List<NoticeDO> list) {
        if (list == null) return new ArrayList<>();
        List<Notice> result = new ArrayList<>(list.size());
        for (NoticeDO item : list) result.add(toEntity(item));
        return result;
    }
}
