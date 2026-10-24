package com.lemnt.system.application.auth.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户分页查询条件对象
 *
 * @author wangxx
 */
public record UserPageQry(
        /**
         * 用户账号
         */
        String userName,
        /**
         * 手机号码
         */
        String phonenumber,
        /**
         * 账号状态（1正常 2停用）
         */
        String status,
        /**
         * 部门ID
         */
        Long deptId,
        /**
         * 角色ID
         */
        Long roleId,
        int pageNum,
        int pageSize,
        LocalDateTime beginTime,
        LocalDateTime endTime
) implements Serializable {
}
