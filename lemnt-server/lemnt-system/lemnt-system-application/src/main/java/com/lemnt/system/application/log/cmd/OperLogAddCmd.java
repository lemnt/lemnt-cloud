package com.lemnt.system.application.log.cmd;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 新增操作日志命令对象
 *
 * @author wangxx
 */
public record OperLogAddCmd(
        /**
         * 模块标题
         */
        String title,
        /**
         * 业务类型
         */
        Integer businessType,
        /**
         * 方法名称
         */
        String method,
        /**
         * 请求方式
         */
        String requestMethod,
        /**
         * 操作类别
         */
        Integer operatorType,
        /**
         * 操作人员
         */
        String operName,
        /**
         * 部门名称
         */
        String deptName,
        /**
         * 请求URL
         */
        String operUrl,
        /**
         * 主机地址
         */
        String operIp,
        /**
         * 操作地点
         */
        String operLocation,
        /**
         * 请求参数
         */
        String operParam,
        /**
         * 返回参数
         */
        String jsonResult,
        /**
         * 操作状态
         */
        Integer status,
        /**
         * 错误消息
         */
        String errorMsg,
        /**
         * 操作发生时间
         */
        LocalDateTime operTime,
        /**
         * 消耗时间（毫秒）
         */
        Long costTime
) implements Serializable {
}
