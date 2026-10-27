package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

/**
 * 修改用户请求
 *
 * @author wangxx
 */
@Schema(description = "修改用户请求")
public record UserUpdateReq(
        @NotNull(message = "用户ID不能为空")
        @Schema(description = "用户ID")
        Long userId,
        @Schema(description = "部门ID")
        Long deptId,
        @Schema(description = "用户昵称")
        String nickName,
        @Schema(description = "用户邮箱")
        String email,
        @Schema(description = "手机号码")
        String phonenumber,
        @Schema(description = "用户性别（0男 1女 2未知）")
        String sex,
        @Schema(description = "账号状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status,
        @Schema(description = "角色ID组")
        List<Long> roleIds,
        @Schema(description = "岗位ID组")
        List<Long> postIds
) implements Serializable {}
