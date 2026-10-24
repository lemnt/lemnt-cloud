package com.lemnt.system.adapter.web.org.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

/**
 * 部门排序更新请求
 *
 * @author wangxx
 */
@Schema(description = "部门排序更新请求")
public record DeptSortUpdateReq(
        @NotEmpty(message = "部门ID列表不能为空")
        @Schema(description = "部门ID列表", example = "[100, 101, 102]")
        List<Long> deptIds,
        @NotEmpty(message = "排序序号列表不能为空")
        @Schema(description = "排序序号列表（与 deptIds 一一对应）", example = "[1, 2, 3]")
        List<Integer> orderNums
) implements Serializable {}
