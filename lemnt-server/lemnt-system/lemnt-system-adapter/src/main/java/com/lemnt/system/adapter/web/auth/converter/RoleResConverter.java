package com.lemnt.system.adapter.web.auth.converter;

import com.lemnt.system.adapter.web.auth.res.RoleRes;
import com.lemnt.system.adapter.web.auth.res.SystemRoleGetInfoRes;
import com.lemnt.system.adapter.web.auth.res.SystemRoleListRes;
import com.lemnt.system.adapter.web.auth.res.SystemRoleOptionselectRes;
import com.lemnt.system.application.auth.dto.RoleDTO;
import com.lemnt.system.domain.auth.model.Role;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * RoleDTO List → RoleInfoRes List
     */
    public static List<SystemRoleListRes> toResListToSystemRoleListRes(List<RoleDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemRoleListRes> result = new ArrayList<>(list.size());
        for (RoleDTO item : list) {
            result.add(toResToSystemRoleListRes(item));
        }
        return result;
    }

    /**
     * RoleDTO → RoleInfoRes
     */
    public static SystemRoleListRes toResToSystemRoleListRes(RoleDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemRoleListRes(
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
    public static SystemRoleListRes toResToSystemRoleListRes(Role entity) {
        if (entity == null) {
            return null;
        }
        return new SystemRoleListRes(
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
     * RoleDTO → RoleInfoRes
     */
    public static SystemRoleGetInfoRes toResToSystemRoleGetInfoRes(RoleDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemRoleGetInfoRes(
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
    public static SystemRoleGetInfoRes toResToSystemRoleGetInfoRes(Role entity) {
        if (entity == null) {
            return null;
        }
        return new SystemRoleGetInfoRes(
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
    public static List<SystemRoleOptionselectRes> toResListToSystemRoleOptionselectRes(List<RoleDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemRoleOptionselectRes> result = new ArrayList<>(list.size());
        for (RoleDTO item : list) {
            result.add(toResToSystemRoleOptionselectRes(item));
        }
        return result;
    }

    /**
     * RoleDTO → RoleInfoRes
     */
    public static SystemRoleOptionselectRes toResToSystemRoleOptionselectRes(RoleDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemRoleOptionselectRes(
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
    public static SystemRoleOptionselectRes toResToSystemRoleOptionselectRes(Role entity) {
        if (entity == null) {
            return null;
        }
        return new SystemRoleOptionselectRes(
                entity.getRoleId(),
                entity.getRoleName(),
                entity.getRoleKey(),
                entity.getRoleSort(),
                entity.getDataScope(),
                entity.getStatus(),
                entity.isFlag()
        );
    }
}
