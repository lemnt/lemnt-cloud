package com.lemnt.system.application.log.qry;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志查询条件对象
 *
 * @author wangxx
 */
public record OperLogPageQry(
        /**
         * 模块标题
         */
        String title,
        /**
         * 业务类型
         */
        Integer businessType,
        /**
         * 业务类型集合
         */
        List<Integer> businessTypes,
        /**
         * 操作状态
         */
        Integer status,
        /**
         * 操作人员
         */
        String operName,
        int pageNum,
        int pageSize,
        LocalDateTime beginTime,
        LocalDateTime endTime
) implements Serializable {
}
