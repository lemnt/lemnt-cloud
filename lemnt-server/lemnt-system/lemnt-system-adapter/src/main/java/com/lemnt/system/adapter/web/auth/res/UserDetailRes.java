package com.lemnt.system.adapter.web.auth.res;

import com.lemnt.system.adapter.web.org.res.PostRes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情响应
 *
 * @author wangxx
 */
@Schema(description = "用户详情响应")
public record UserDetailRes(
        @Schema(description = "用户信息")
        UserDetail detail,
        @Schema(description = "角色ID组")
        List<Long> roleIds,
        @Schema(description = "岗位ID组")
        List<Long> postIds,
        @Schema(description = "角色列表")
        List<RoleRes> roles,
        @Schema(description = "岗位列表")
        List<PostRes> posts
) implements Serializable {
    /**
     * 用户详情信息
     */
    @Schema(description = "用户详情信息")
    public record UserDetail(
            @Schema(description = "用户ID")
            Long userId,
            @Schema(description = "部门ID")
            Long deptId,
            @Schema(description = "用户账号")
            String userName,
            @Schema(description = "用户昵称")
            String nickName,
            @Schema(description = "用户邮箱")
            String email,
            @Schema(description = "手机号码")
            String phonenumber,
            @Schema(description = "用户性别（0男 1女 2未知）")
            String sex,
            @Schema(description = "用户头像")
            String avatar,
            @Schema(description = "账号状态（1正常 2停用）")
            String status,
            @Schema(description = "最后登录IP")
            String loginIp,
            @Schema(description = "最后登录时间")
            LocalDateTime loginDate,
            @Schema(description = "创建者")
            String createBy,
            @Schema(description = "创建时间")
            LocalDateTime createTime
    ) implements Serializable {
    }
}
