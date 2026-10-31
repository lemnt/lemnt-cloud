package com.lemnt.system.adapter.web.auth.res;

import com.lemnt.system.application.auth.dto.RoleDTO;
import com.lemnt.system.application.org.dto.DeptDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户基础信息响应
 *
 * @author wangxx
 */
@Schema(description = "用户基础信息响应")
public record SystemRoleUserUnallocatedListRes(
        /**
 * 用户ID
 */
        @Schema(description = "用户ID")
        Long userId,
        /**
 * 部门ID
 */
        @Schema(description = "部门ID")
        Long deptId,
        /**
 * 用户账号
 */
        @Schema(description = "用户账号")
        String userName,
        /**
 * 用户昵称
 */
        @Schema(description = "用户昵称")
        String nickName,
        /**
 * 用户邮箱
 */
        @Schema(description = "用户邮箱")
        String email,
        /**
 * 手机号码
 */
        @Schema(description = "手机号码")
        String phonenumber,
        /**
 * 用户性别（0男 1女 2未知）
 */
        @Schema(description = "用户性别（0男 1女 2未知）")
        String sex,
        /**
 * 用户头像
 */
        @Schema(description = "用户头像")
        String avatar,
        /**
 * 账号状态（1正常 2停用）
 */
        @Schema(description = "账号状态（1正常 2停用）")
        String status,
        /**
 * 最后登录IP
 */
        @Schema(description = "最后登录IP")
        String loginIp,
        /**
 * 最后登录时间
 */
        @Schema(description = "最后登录时间")
        LocalDateTime loginDate,
        /**
 * 创建者
 */
        @Schema(description = "创建者")
        String createBy,
        /**
 * 创建时间
 */
        @Schema(description = "创建时间")
        LocalDateTime createTime,
        /**
 * 角色列表
 */
        @Schema(description = "角色列表")
        List<RoleDTO> roles,
        /**
 * 部门
 */
        @Schema(description = "部门")
        DeptDTO dept
) implements Serializable {}
