package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户分页查询条件对象
 *
 * @author wangxx
 */
@Schema(description = "用户分页查询条件")
public record UserPageReq(
        /**
 * 用户账号
 */
        @Schema(description = "用户账号")
        String userName,
        /**
 * 手机号码
 */
        @Schema(description = "手机号码")
        String phonenumber,
        /**
 * 账号状态（1正常 2停用）
 */
        @Schema(description = "账号状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status,
        /**
 * 部门ID
 */
        @Schema(description = "部门ID")
        Long deptId,
        /**
 * 角色ID
 */
        @Schema(description = "角色ID")
        Long roleId,
        @Schema(description = "页码")
        Integer pageNum,
        @Schema(description = "每页数量")
        Integer pageSize,
        @Schema(description = "开始时间")
        LocalDateTime beginTime,
        @Schema(description = "结束时间")
        LocalDateTime endTime
) implements Serializable {
    public UserPageReq {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
    }
}
