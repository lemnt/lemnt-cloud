package com.lemnt.system.adapter.web.log.converter;

import com.lemnt.system.adapter.web.log.res.OperLogRes;
import com.lemnt.system.application.log.dto.OperLogDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志响应对象组装器
 *
 * @author wangxx
 */
public class OperLogResConverter {
    /**
     * OperLogDTO → OperLogInfoRes
     */
    public static OperLogRes toRes(OperLogDTO dto) {
        if (dto == null) {
            return null;
        }
        return new OperLogRes(
                dto.getOperId(),
                dto.getTitle(),
                dto.getBusinessType(),
                dto.getMethod(),
                dto.getRequestMethod(),
                dto.getOperatorType(),
                dto.getOperName(),
                dto.getDeptName(),
                dto.getOperUrl(),
                dto.getOperIp(),
                dto.getOperLocation(),
                dto.getOperParam(),
                dto.getJsonResult(),
                dto.getStatus(),
                dto.getErrorMsg(),
                dto.getOperTime(),
                dto.getCostTime()
        );
    }

    /**
     * OperLogDTO List → OperLogInfoRes List
     */
    public static List<OperLogRes> toResList(List<OperLogDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<OperLogRes> result = new ArrayList<>(list.size());
        for (OperLogDTO item : list) {
            result.add(toRes(item));
        }
        return result;
    }
}
