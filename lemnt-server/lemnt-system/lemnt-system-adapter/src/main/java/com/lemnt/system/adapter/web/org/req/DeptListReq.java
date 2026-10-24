package com.lemnt.system.adapter.web.org.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 部门列表查询条件
 *
 * @author wangxx
 */
@Schema(description = "部门列表查询条件")
public record DeptListReq(
        /**
         * 部门名称
         */
        @Schema(description = "部门名称")
        String deptName,
        /**
         * 部门状态（1正常 2停用）
         */
        @Schema(description = "部门状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status
) implements Serializable {}
