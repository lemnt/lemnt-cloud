package com.lemnt.system.adapter.web.auth.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 角色部门树列表响应
 *
 * @author wangxx
 */
@Schema(description = "角色部门树列表响应")
public record RoleDeptTreeRes(
        @Schema(description = "已选中部门ID列表")
        List<Long> checkedKeys,
        @Schema(description = "部门树列表")
        List<TreeSelectRes> depts
) implements Serializable {}
