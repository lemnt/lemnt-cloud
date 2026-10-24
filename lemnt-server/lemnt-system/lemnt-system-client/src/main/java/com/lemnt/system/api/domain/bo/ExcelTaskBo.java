package com.lemnt.system.api.domain.bo;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.lemnt.common.api.domain.bo.BaseBO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 表格任务对象 sys_excel_task
 *
 * @author wangxx
 * @date 2026-01-20
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ExcelTaskBo extends BaseBO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 任务状态 0 等待执行 1执行中 2执行成功 3执行失败 4已中断
     */
    private Integer status;

    /**
     * 持续时间
     */
    private Long duration;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 导出进度
     */
    private BigDecimal progress;

    /**
     * 文件路径
     */
    private String url;

    /**
     * 错误信息
     */
    private String message;
}
