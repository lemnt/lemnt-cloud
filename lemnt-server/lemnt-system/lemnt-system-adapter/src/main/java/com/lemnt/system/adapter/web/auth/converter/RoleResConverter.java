package com.lemnt.system.adapter.web.auth.converter;

import java.util.ArrayList;
import java.util.List;

import com.lemnt.system.adapter.web.auth.res.RoleRes;
import com.lemnt.system.application.auth.dto.RoleDTO;
import com.lemnt.system.domain.auth.model.Role;

/**
 * 角色响应对象组装器
 *
 * @author wangxx
 */
public class RoleResConverter {

    /**
     * RoleDTO → RoleInfoRes
     */
    public static RoleRes toRes(RoleDTO dto) {
        if (dto == null) {
            return null;
        }
        return new RoleRes(
                dto.getRoleId(),
                dto.getRoleName(),
                dto.getRoleKey(),
                dto.getRoleSort(),
                dto.getDataScope(),
                dto.getStatus(),
                dto.isFlag()
        );
    }

    /**
     * Role Entity → RoleInfoRes
     */
    public static RoleRes toRes(Role entity) {
        if (entity == null) {
            return null;
        }
        return new RoleRes(
                entity.getRoleId(),
                entity.getRoleName(),
                entity.getRoleKey(),
                entity.getRoleSort(),
                entity.getDataScope(),
                entity.getStatus(),
                entity.isFlag()
        );
    }

    /**
     * RoleDTO List → RoleInfoRes List
     */
    public static List<RoleRes> toResList(List<RoleDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<RoleRes> result = new ArrayList<>(list.size());
        for (RoleDTO item : list) {
            result.add(toRes(item));
        }
        return result;
    }
}
