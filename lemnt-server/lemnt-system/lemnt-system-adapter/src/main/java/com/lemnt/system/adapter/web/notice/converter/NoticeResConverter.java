package com.lemnt.system.adapter.web.notice.converter;

import com.lemnt.system.adapter.web.notice.res.NoticeRes;
import com.lemnt.system.adapter.web.notice.res.SystemNoticeGetInfoRes;
import com.lemnt.system.adapter.web.notice.res.SystemNoticeListRes;
import com.lemnt.system.application.notice.dto.NoticeDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知公告响应对象组装器
 *
 * @author wangxx
 */
public class NoticeResConverter {
    /**
     * NoticeDTO → NoticeInfoRes
     */
    public static NoticeRes toRes(NoticeDTO dto) {
        if (dto == null) {
            return null;
        }
        return new NoticeRes(
                dto.getNoticeId(),
                dto.getNoticeTitle(),
                dto.getNoticeType(),
                dto.getNoticeContent(),
                dto.getStatus(),
                dto.getIsRead(),
                dto.getCreateBy(),
                dto.getCreateTime(),
                dto.getUpdateBy(),
                dto.getUpdateTime(),
                dto.getRemark()
        );
    }

    /**
     * NoticeDTO List → NoticeInfoRes List
     */
    public static List<NoticeRes> toResList(List<NoticeDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<NoticeRes> result = new ArrayList<>(list.size());
        for (NoticeDTO item : list) {
            result.add(toRes(item));
        }
        return result;
    }

    /**
     * NoticeDTO List → NoticeInfoRes List
     */
    public static List<SystemNoticeListRes> toResListToSystemNoticeListRes(List<NoticeDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemNoticeListRes> result = new ArrayList<>(list.size());
        for (NoticeDTO item : list) {
            result.add(toResToSystemNoticeListRes(item));
        }
        return result;
    }

    /**
     * NoticeDTO → NoticeInfoRes
     */
    public static SystemNoticeListRes toResToSystemNoticeListRes(NoticeDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemNoticeListRes(
                dto.getNoticeId(),
                dto.getNoticeTitle(),
                dto.getNoticeType(),
                dto.getNoticeContent(),
                dto.getStatus(),
                dto.getIsRead(),
                dto.getCreateBy(),
                dto.getCreateTime(),
                dto.getUpdateBy(),
                dto.getUpdateTime(),
                dto.getRemark()
        );
    }

    /**
     * NoticeDTO → NoticeInfoRes
     */
    public static SystemNoticeGetInfoRes toResToSystemNoticeGetInfoRes(NoticeDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemNoticeGetInfoRes(
                dto.getNoticeId(),
                dto.getNoticeTitle(),
                dto.getNoticeType(),
                dto.getNoticeContent(),
                dto.getStatus(),
                dto.getIsRead(),
                dto.getCreateBy(),
                dto.getCreateTime(),
                dto.getUpdateBy(),
                dto.getUpdateTime(),
                dto.getRemark()
        );
    }
}
