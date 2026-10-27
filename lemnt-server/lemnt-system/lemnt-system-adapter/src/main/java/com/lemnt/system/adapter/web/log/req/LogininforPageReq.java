package com.lemnt.system.adapter.web.log.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志查询条件对象
 *
 * @author wangxx
 */
@Schema(description = "登录日志查询条件")
public record LogininforPageReq(
        /**
 * 用户账号
 */
        @Schema(description = "用户账号")
        String userName,
        /**
 * 登录IP地址
 */
        @Schema(description = "登录IP地址")
        String ipaddr,
        /**
 * 登录状态（1成功 2失败）
 */
        @Schema(description = "登录状态（1成功 2失败）")
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
    public LogininforPageReq {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
    }
}
