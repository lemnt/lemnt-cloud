package com.lemnt.system.application.auth.dto;

import com.lemnt.system.application.org.dto.PostDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 用户 DTO
 *
 * @author wangxx
 */
@Data
public class UserDetailDTO {

    /**
     * 用户信息
     */
    private UserDetail detail;

    /**
     * 角色ID组
     */
    private List<Long> roleIds;

    /**
     * 岗位ID组
     */
    private List<Long> postIds;

    /**
     * 角色列表
     */
    private List<RoleDTO> roles;

    /**
     * 岗位列表
     */
    private List<PostDTO> posts;

    /**
     * 用户信息
     */
    @Data
    public static class UserDetail {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 部门ID
         */
        private Long deptId;

        /**
         * 用户账号
         */
        private String userName;

        /**
         * 用户昵称
         */
        private String nickName;

        /**
         * 用户邮箱
         */
        private String email;

        /**
         * 手机号码
         */
        private String phonenumber;

        /**
         * 用户性别（0男 1女 2未知）
         */
        private String sex;

        /**
         * 用户头像
         */
        private String avatar;

        /**
         * 账号状态（1正常 2停用）
         */
        private String status;

        /**
         * 最后登录IP
         */
        private String loginIp;

        /**
         * 最后登录时间
         */
        private LocalDateTime loginDate;

        /**
         * 创建者
         */
        private String createBy;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }
}
