package com.lemnt.system.adapter.web.log.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志信息响应
 *
 * @author wangxx
 */
@Schema(description = "登录日志信息响应")
public record LogininforRes(
        /**
 * 访问ID
 */
        @Schema(description = "访问ID")
        Long infoId,
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
        String status,
        /**
 * 提示消息
 */
        @Schema(description = "提示消息")
        String msg,
        /**
 * 访问时间
 */
        @Schema(description = "访问时间")
        LocalDateTime accessTime
) implements Serializable {}
