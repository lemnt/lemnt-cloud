package com.lemnt.system.adapter.web.log.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志查询条件对象
 *
 * @author wangxx
 */
@Schema(description = "操作日志查询条件")
public record OperLogPageReq(
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
         * 业务类型集合
         */
        @Schema(description = "业务类型集合")
        List<Integer> businessTypes,
        /**
         * 操作状态
         */
        @Schema(description = "操作状态（1成功 2失败）")
        @Min(value = 1, message = "状态值只能为1或2")
        @Max(value = 2, message = "状态值只能为1或2")
        Integer status,
        /**
         * 操作人员
         */
        @Schema(description = "操作人员")
        String operName,
        @Schema(description = "页码")
        Integer pageNum,
        @Schema(description = "每页数量")
        Integer pageSize,
        @Schema(description = "开始时间")
        LocalDateTime beginTime,
        @Schema(description = "结束时间")
        LocalDateTime endTime
) implements Serializable {

    public OperLogPageReq {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
    }
}
