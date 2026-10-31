package com.lemnt.system.adapter.web.org.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位信息响应
 *
 * @author wangxx
 */
@Schema(description = "岗位信息响应")
public record SystemPostListRes(
        /**
 * 岗位ID
 */
        @Schema(description = "岗位ID")
        Long postId,
        /**
 * 岗位编码
 */
        @Schema(description = "岗位编码")
        String postCode,
        /**
 * 岗位名称
 */
        @Schema(description = "岗位名称")
        String postName,
        /**
 * 岗位排序
 */
        @Schema(description = "岗位排序")
        Integer postSort,
        /**
 * 状态（1正常 2停用）
 */
        @Schema(description = "状态（1正常 2停用）")
        String status,
        /**
 * 备注
 */
        @Schema(description = "备注")
        String remark,
        /**
 * 创建时间
 */
        @Schema(description = "创建时间")
        LocalDateTime createTime
) implements Serializable {}
