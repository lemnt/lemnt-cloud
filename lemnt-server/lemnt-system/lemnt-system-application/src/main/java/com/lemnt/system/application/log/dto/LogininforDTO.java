package com.lemnt.system.application.log.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志 DTO
 *
 * @author wangxx
 */
@Data
public class LogininforDTO {

    /**
     * 访问ID
     */
    private Long infoId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录状态（1成功 2失败）
     */
    private String status;

    /**
     * 提示消息
     */
    private String msg;

    /**
     * 访问时间
     */
    private LocalDateTime accessTime;
}
