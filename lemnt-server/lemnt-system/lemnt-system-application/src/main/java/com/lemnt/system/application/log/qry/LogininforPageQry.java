package com.lemnt.system.application.log.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志查询条件对象
 *
 * @author wangxx
 */
public record LogininforPageQry(
        /**
         * 用户账号
         */
        String userName,
        /**
         * 登录IP地址
         */
        String ipaddr,
        /**
         * 登录状态（1成功 2失败）
         */
        String status,
        int pageNum,
        int pageSize,
        LocalDateTime beginTime,
        LocalDateTime endTime
) implements Serializable {
}
