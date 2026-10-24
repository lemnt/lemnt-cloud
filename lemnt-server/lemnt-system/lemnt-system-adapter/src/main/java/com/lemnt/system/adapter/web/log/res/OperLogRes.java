package com.lemnt.system.adapter.web.log.res;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 操作日志信息响应
 *
 * @author wangxx
 */
@Schema(description = "操作日志信息响应")
public record OperLogRes(
        /**
         * 日志主键
         */
        @Schema(description = "日志主键")
        Long operId,
        /**
         * 模块标题
         */
        @Schema(description = "模块标题")
        String title,
        /**
         * 业务类型
         */
        @Schema(description = "业务类型")
        Integer businessType,
        /**
         * 方法名称
         */
        @Schema(description = "方法名称")
        String method,
        /**
         * 请求方式
         */
        @Schema(description = "请求方式")
        String requestMethod,
        /**
         * 操作类别
         */
        @Schema(description = "操作类别")
        Integer operatorType,
        /**
         * 操作人员
         */
        @Schema(description = "操作人员")
        String operName,
        /**
         * 部门名称
         */
        @Schema(description = "部门名称")
        String deptName,
        /**
         * 请求URL
         */
        @Schema(description = "请求URL")
        String operUrl,
        /**
         * 主机地址
         */
        @Schema(description = "主机地址")
        String operIp,
        /**
         * 操作地点
         */
        @Schema(description = "操作地点")
        String operLocation,
        /**
         * 请求参数
         */
        @Schema(description = "请求参数")
        String operParam,
        /**
         * 返回参数
         */
        @Schema(description = "返回参数")
        String jsonResult,
        /**
         * 操作状态
         */
        @Schema(description = "操作状态")
        Integer status,
        /**
         * 错误消息
         */
        @Schema(description = "错误消息")
        String errorMsg,
        /**
         * 操作时间
         */
        @Schema(description = "操作时间")
        LocalDateTime operTime,
        /**
         * 消耗时间
         */
        @Schema(description = "消耗时间（毫秒）")
        Long costTime
) implements Serializable {}
