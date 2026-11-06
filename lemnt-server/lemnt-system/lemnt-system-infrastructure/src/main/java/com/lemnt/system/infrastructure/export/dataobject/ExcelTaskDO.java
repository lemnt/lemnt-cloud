package com.lemnt.system.infrastructure.export.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 表格任务对象 sys_excel_task
 *
 * @author wangxx
 * @date 2026-01-20
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_excel_task")
public class ExcelTaskDO extends BaseDO {
    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
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
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic(value = "0", delval = "2")
    private String delFlag;

    /**
     * 错误信息
     */
    private String message;
}
