package com.lemnt.system.domain.log.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志聚合根 sys_logininfor
 *
 * @author wangxx
 */
@Data
@Accessors(chain = true)
public class Logininfor implements Serializable {
    @Serial
    private static final long serialVersionUID = 3665044417663228374L;

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
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

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

    /**
     * 开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}