package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色分页查询条件
 *
 * @author wangxx
 */
@Schema(description = "角色分页查询条件")
public record RolePageReq(
        /**
         * 角色名称
         */
        @Schema(description = "角色名称")
        String roleName,
        /**
         * 角色权限标识
         */
        @Schema(description = "角色权限标识")
        String roleKey,
        /**
         * 角色状态（1正常 2停用）
         */
        @Schema(description = "角色状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status,
        @Schema(description = "页码")
        Integer pageNum,
        @Schema(description = "每页数量")
        Integer pageSize,
        @Schema(description = "开始时间")
        LocalDateTime beginTime,
        @Schema(description = "结束时间")
        LocalDateTime endTime
) implements Serializable {

    public RolePageReq {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
    }
}
