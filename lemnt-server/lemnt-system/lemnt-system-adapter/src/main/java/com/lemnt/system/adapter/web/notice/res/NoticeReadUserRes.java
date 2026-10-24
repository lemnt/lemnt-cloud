package com.lemnt.system.adapter.web.notice.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告已读记录聚合根 sys_notice_read
 *
 * @author wangxx
 */
@Schema(description = "公告已读用户响应")
public record NoticeReadUserRes(
        /**
         * 用户ID
         */
        @Schema(description = "用户ID")
        Long userId,
        /**
         * 用户名
         */
        @Schema(description = "用户名")
        String userName,
        /**
         * 昵称
         */
        @Schema(description = "昵称")
        String nickName,
        /**
         * 部门名称
         */
        @Schema(description = "部门名称")
        String deptName,
        /**
         * 手机号
         */
        @Schema(description = "手机号")
        String phonenumber,
        /**
         * 阅读时间
         */
        @Schema(description = "阅读时间")
        LocalDateTime readTime
) implements Serializable {
}
