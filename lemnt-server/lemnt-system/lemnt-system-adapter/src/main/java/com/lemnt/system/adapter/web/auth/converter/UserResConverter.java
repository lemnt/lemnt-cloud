package com.lemnt.system.adapter.web.auth.converter;

import com.lemnt.system.adapter.web.auth.res.SystemRoleUserAllocatedListRes;
import com.lemnt.system.adapter.web.auth.res.SystemRoleUserUnallocatedListRes;
import com.lemnt.system.adapter.web.auth.res.SystemUserListRes;
import com.lemnt.system.adapter.web.auth.res.UserDetailRes;
import com.lemnt.system.adapter.web.auth.res.UserRes;
import com.lemnt.system.adapter.web.org.converter.PostResConverter;
import com.lemnt.system.application.auth.dto.UserDTO;
import com.lemnt.system.application.auth.dto.UserDetailDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户响应对象组装器
 *
 * @author wangxx
 */
public class UserResConverter {
    /**
     * UserDTO → UserBaseInfoRes
     */
    public static UserRes toRes(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        return new UserRes(
                dto.getUserId(),
                dto.getDeptId(),
                dto.getUserName(),
                dto.getNickName(),
                dto.getEmail(),
                dto.getPhonenumber(),
                dto.getSex(),
                dto.getAvatar(),
                dto.getStatus(),
                dto.getLoginIp(),
                dto.getLoginDate(),
                dto.getCreateBy(),
                dto.getCreateTime(),
                dto.getRoles(),
                dto.getDept()
        );
    }

    /**
     * UserDTO List → UserBaseInfoRes List
     */
    public static List<UserRes> toResList(List<UserDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<UserRes> result = new ArrayList<>(list.size());
        for (UserDTO item : list) {
            result.add(toRes(item));
        }
        return result;
    }

    /**
     * UserDetailDTO → UserDetailInfoRes
     */
    public static UserDetailRes toDetailRes(UserDetailDTO dto) {
        if (dto == null) {
            return null;
        }
        return new UserDetailRes(
                toUserDetail(dto.getDetail()),
                dto.getRoleIds(),
                dto.getPostIds(),
                RoleResConverter.toResList(dto.getRoles()),
                PostResConverter.toResList(dto.getPosts())
        );
    }

    /**
     * UserDetailDTO.UserDetail → UserDetailInfoRes.UserDetailInfo
     */
    private static UserDetailRes.UserDetail toUserDetail(UserDetailDTO.UserDetail detail) {
        if (detail == null) {
            return null;
        }
        return new UserDetailRes.UserDetail(
                detail.getUserId(),
                detail.getDeptId(),
                detail.getUserName(),
                detail.getNickName(),
                detail.getEmail(),
                detail.getPhonenumber(),
                detail.getSex(),
                detail.getAvatar(),
                detail.getStatus(),
                detail.getLoginIp(),
                detail.getLoginDate(),
                detail.getCreateBy(),
                detail.getCreateTime()
        );
    }

    /**
     * UserDTO List → UserBaseInfoRes List
     */
    public static List<SystemUserListRes> toResListToSystemUserListRes(List<UserDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemUserListRes> result = new ArrayList<>(list.size());
        for (UserDTO item : list) {
            result.add(toResToSystemUserListRes(item));
        }
        return result;
    }

    /**
     * UserDTO → UserBaseInfoRes
     */
    public static SystemUserListRes toResToSystemUserListRes(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemUserListRes(
                dto.getUserId(),
                dto.getDeptId(),
                dto.getUserName(),
                dto.getNickName(),
                dto.getEmail(),
                dto.getPhonenumber(),
                dto.getSex(),
                dto.getAvatar(),
                dto.getStatus(),
                dto.getLoginIp(),
                dto.getLoginDate(),
                dto.getCreateBy(),
                dto.getCreateTime(),
                dto.getRoles(),
                dto.getDept()
        );
    }

    /**
     * UserDTO List → UserBaseInfoRes List
     */
    public static List<SystemRoleUserAllocatedListRes> toResListToSystemRoleUserAllocatedListRes(List<UserDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemRoleUserAllocatedListRes> result = new ArrayList<>(list.size());
        for (UserDTO item : list) {
            result.add(toResToSystemRoleUserAllocatedListRes(item));
        }
        return result;
    }

    /**
     * UserDTO → UserBaseInfoRes
     */
    public static SystemRoleUserAllocatedListRes toResToSystemRoleUserAllocatedListRes(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemRoleUserAllocatedListRes(
                dto.getUserId(),
                dto.getDeptId(),
                dto.getUserName(),
                dto.getNickName(),
                dto.getEmail(),
                dto.getPhonenumber(),
                dto.getSex(),
                dto.getAvatar(),
                dto.getStatus(),
                dto.getLoginIp(),
                dto.getLoginDate(),
                dto.getCreateBy(),
                dto.getCreateTime(),
                dto.getRoles(),
                dto.getDept()
        );
    }

    /**
     * UserDTO List → UserBaseInfoRes List
     */
    public static List<SystemRoleUserUnallocatedListRes> toResListToSystemRoleUserUnallocatedListRes(List<UserDTO> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<SystemRoleUserUnallocatedListRes> result = new ArrayList<>(list.size());
        for (UserDTO item : list) {
            result.add(toResToSystemRoleUserUnallocatedListRes(item));
        }
        return result;
    }

    /**
     * UserDTO → UserBaseInfoRes
     */
    public static SystemRoleUserUnallocatedListRes toResToSystemRoleUserUnallocatedListRes(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SystemRoleUserUnallocatedListRes(
                dto.getUserId(),
                dto.getDeptId(),
                dto.getUserName(),
                dto.getNickName(),
                dto.getEmail(),
                dto.getPhonenumber(),
                dto.getSex(),
                dto.getAvatar(),
                dto.getStatus(),
                dto.getLoginIp(),
                dto.getLoginDate(),
                dto.getCreateBy(),
                dto.getCreateTime(),
                dto.getRoles(),
                dto.getDept()
        );
    }
}
