package com.lemnt.system.adapter.web.org.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门信息响应
 *
 * @author wangxx
 */
@Schema(description = "部门信息响应")
public record SystemDeptGetInfoRes(
        /**
 * 部门ID
 */
        @Schema(description = "部门ID")
        Long deptId,
        /**
 * 父部门ID
 */
        @Schema(description = "父部门ID")
        Long parentId,
        /**
 * 祖级列表
 */
        @Schema(description = "祖级列表")
        String ancestors,
        /**
 * 部门名称
 */
        @Schema(description = "部门名称")
        String deptName,
        /**
 * 显示顺序
 */
        @Schema(description = "显示顺序")
        Integer orderNum,
        /**
 * 负责人
 */
        @Schema(description = "负责人")
        String leader,
        /**
 * 联系电话
 */
        @Schema(description = "联系电话")
        String phone,
        /**
 * 邮箱
 */
        @Schema(description = "邮箱")
        String email,
        /**
 * 部门状态（1正常 2停用）
 */
        @Schema(description = "部门状态（1正常 2停用）")
        String status,
        /**
 * 父部门名称
 */
        @Schema(description = "父部门名称")
        String parentName,
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
 * 子部门列表
 */
        @Schema(description = "子部门列表")
        List<SystemDeptGetInfoRes> children
) implements Serializable {}
