package com.lemnt.system.adapter.web.org.converter;

import java.util.ArrayList;
import java.util.List;

import com.lemnt.system.adapter.web.org.res.DeptRes;
import com.lemnt.system.application.org.dto.DeptDTO;

/**
 * 部门响应对象组装器
 *
 * @author wangxx
 */
public class DeptResConverter {

    /**
     * DeptDTO → DeptInfoRes
     */
    public static DeptRes toRes(DeptDTO dto) {
        if (dto == null) {
            return null;
        }
        return new DeptRes(
                dto.getDeptId(),
                dto.getParentId(),
                dto.getAncestors(),
                dto.getDeptName(),
                dto.getOrderNum(),
                dto.getLeader(),
                dto.getPhone(),
                dto.getEmail(),
                dto.getStatus(),
                dto.getParentName(),
                dto.getCreateBy(),
                dto.getCreateTime(),
                toResList(dto.getChildren())
        );
    }

    /**
     * DeptDTO List → DeptInfoRes List
     */
    public static List<DeptRes> toResList(List<DeptDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<DeptRes> result = new ArrayList<>(list.size());
        for (DeptDTO item : list) {
            result.add(toRes(item));
        }
        return result;
    }
}
