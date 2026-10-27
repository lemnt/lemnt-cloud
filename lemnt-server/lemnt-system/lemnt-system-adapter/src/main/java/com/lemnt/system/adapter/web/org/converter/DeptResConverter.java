package com.lemnt.system.adapter.web.org.converter;

import com.lemnt.system.adapter.web.org.res.DeptRes;
import com.lemnt.system.adapter.web.org.res.SystemDeptExcludeChildRes;
import com.lemnt.system.adapter.web.org.res.SystemDeptGetInfoRes;
import com.lemnt.system.adapter.web.org.res.SystemDeptListRes;
import com.lemnt.system.application.org.dto.DeptDTO;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * DeptDTO List → DeptInfoRes List
     */
    public static List<SystemDeptListRes> toResListToSystemDeptListRes(List<DeptDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemDeptListRes> result = new ArrayList<>(list.size());
        for (DeptDTO item : list) {
            result.add(toResToSystemDeptListRes(item));
        }
        return result;
    }

    /**
     * DeptDTO → DeptInfoRes
     */
    public static SystemDeptListRes toResToSystemDeptListRes(DeptDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemDeptListRes(
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
                toResListToSystemDeptListRes(dto.getChildren())
        );
    }

    /**
     * DeptDTO List → DeptInfoRes List
     */
    public static List<SystemDeptExcludeChildRes> toResListToSystemDeptExcludeChildRes(List<DeptDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemDeptExcludeChildRes> result = new ArrayList<>(list.size());
        for (DeptDTO item : list) {
            result.add(toResToSystemDeptExcludeChildRes(item));
        }
        return result;
    }

    /**
     * DeptDTO → DeptInfoRes
     */
    public static SystemDeptExcludeChildRes toResToSystemDeptExcludeChildRes(DeptDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemDeptExcludeChildRes(
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
                toResListToSystemDeptExcludeChildRes(dto.getChildren())
        );
    }

    /**
     * DeptDTO → DeptInfoRes
     */
    public static SystemDeptGetInfoRes toResToSystemDeptGetInfoRes(DeptDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemDeptGetInfoRes(
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
                toResListToSystemDeptGetInfoRes(dto.getChildren())
        );
    }

    /**
     * DeptDTO List → DeptInfoRes List
     */
    public static List<SystemDeptGetInfoRes> toResListToSystemDeptGetInfoRes(List<DeptDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemDeptGetInfoRes> result = new ArrayList<>(list.size());
        for (DeptDTO item : list) {
            result.add(toResToSystemDeptGetInfoRes(item));
        }
        return result;
    }
}
