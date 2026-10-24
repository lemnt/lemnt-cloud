package com.lemnt.system.application.log.cmd;

import java.io.Serializable;

/**
 * 新增登录日志命令对象
 *
 * @author wangxx
 */
public record LogininforAddCmd(
        /**
         * 用户账号
         */
        String userName,
        /**
         * 登录IP地址
         */
        String ipaddr,
        /**
         * 登录地点
         */
        String loginLocation,
        /**
         * 浏览器类型
         */
        String browser,
        /**
         * 操作系统
         */
        String os,
        /**
         * 登录状态（1成功 2失败）
         */
        String status,
        /**
         * 提示消息
         */
        String msg
) implements Serializable {
}
