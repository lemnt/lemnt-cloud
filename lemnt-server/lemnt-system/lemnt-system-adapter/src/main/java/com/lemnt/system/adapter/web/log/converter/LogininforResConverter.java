package com.lemnt.system.adapter.web.log.converter;

import com.lemnt.system.adapter.web.log.res.LogininforRes;
import com.lemnt.system.application.log.dto.LogininforDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录日志响应对象组装器
 *
 * @author wangxx
 */
public class LogininforResConverter {
    /**
     * LogininforDTO → LogininforInfoRes
     */
    public static LogininforRes toRes(LogininforDTO dto) {
        if (dto == null) {
            return null;
        }
        return new LogininforRes(
                dto.getInfoId(),
                dto.getUserName(),
                dto.getIpaddr(),
                dto.getStatus(),
                dto.getMsg(),
                dto.getAccessTime()
        );
    }

    /**
     * LogininforDTO List → LogininforInfoRes List
     */
    public static List<LogininforRes> toResList(List<LogininforDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<LogininforRes> result = new ArrayList<>(list.size());
        for (LogininforDTO item : list) {
            result.add(toRes(item));
        }
        return result;
    }
}
