package com.lemnt.system.application.org.qry;

import java.io.Serializable;

/**
 * 部门列表查询条件
 *
 * @author wangxx
 */
public record DeptListQry(
        /**
         * 部门名称
         */
        String deptName,
        /**
         * 部门状态（1正常 2停用）
         */
        String status
) implements Serializable {
}
