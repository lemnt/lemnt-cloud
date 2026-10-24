package com.lemnt.system.adapter.web.notice.converter;

import com.lemnt.system.adapter.web.notice.res.NoticeReadUserRes;
import com.lemnt.system.adapter.web.notice.res.NoticeRes;
import com.lemnt.system.application.notice.dto.NoticeDTO;
import com.lemnt.system.application.notice.dto.NoticeReadUserDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知公告响应对象组装器
 *
 * @author wangxx
 */
public class NoticeReadResConverter {

    /**
     * NoticeDTO → NoticeInfoRes
     */
    public static NoticeReadUserRes toRes(NoticeReadUserDTO dto) {
        if (dto == null) {
            return null;
        }
        return new NoticeReadUserRes(
                dto.getUserId(),
                dto.getUserName(),
                dto.getNickName(),
                dto.getDeptName(),
                dto.getPhonenumber(),
                dto.getReadTime()
        );
    }

    /**
     * NoticeDTO List → NoticeInfoRes List
     */
    public static List<NoticeReadUserRes> toResList(List<NoticeReadUserDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<NoticeReadUserRes> result = new ArrayList<>(list.size());
        for (NoticeReadUserDTO item : list) {
            result.add(toRes(item));
        }
        return result;
    }
}
