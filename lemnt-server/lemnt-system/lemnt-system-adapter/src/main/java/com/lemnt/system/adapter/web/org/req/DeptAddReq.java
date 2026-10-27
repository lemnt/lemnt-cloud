package com.lemnt.system.adapter.web.org.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 新增部门请求
 *
 * @author wangxx
 */
@Schema(description = "新增部门请求")
public record DeptAddReq(
        @NotNull(message = "父部门ID不能为空")
        @Schema(description = "父部门ID")
        Long parentId,
        @NotBlank(message = "部门名称不能为空")
        @Schema(description = "部门名称")
        String deptName,
        @Schema(description = "显示顺序")
        Integer orderNum,
        @Schema(description = "负责人")
        String leader,
        @Schema(description = "联系电话")
        String phone,
        @Schema(description = "邮箱")
        String email,
        @Schema(description = "部门状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status
) implements Serializable {}
