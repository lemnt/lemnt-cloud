package com.lemnt.system.adapter.web.org.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 新增岗位请求
 *
 * @author wangxx
 */
@Schema(description = "新增岗位请求")
public record PostAddReq(
        @NotBlank(message = "岗位名称不能为空")
        @Schema(description = "岗位名称")
        String postName,
        @NotBlank(message = "岗位编码不能为空")
        @Schema(description = "岗位编码")
        String postCode,
        @Schema(description = "岗位排序")
        Integer postSort,
        @Schema(description = "状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status,
        @Schema(description = "备注")
        String remark
) implements Serializable {}
