package com.lemnt.common.excel.service;

import com.lemnt.common.core.utils.DateUtils;
import com.lemnt.common.excel.api.RemoteExcelTaskService;
import com.lemnt.common.excel.constant.TaskStatusEnum;
import com.lemnt.common.excel.domain.to.ExcelTaskTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 任务生命周期模板
 * <p>
 * 统一处理导入/导出任务的通用生命周期（启动、进度、完成、失败），
 * 消除 BaseExcelTaskServiceImpl 中 import/export 成对方法的重复代码。
 * </p>
 *
 * @author wangxx
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskLifecycleTemplate {

    private final RemoteExcelTaskService remoteExcelTaskService;

    /**
     * 启动任务
     */
    public void startTask(Long taskId) {
        execute(taskId, task -> {
            task.setStartTime(LocalDateTime.now());
            task.setStatus(TaskStatusEnum.DOING.getStatus());
        });
    }

    /**
     * 更新导入进度
     */
    public void updateProgress(Long taskId, Integer count, Integer totalNum) {
        execute(taskId, task -> {
            task.setProgress(new BigDecimal(count.toString())
                    .divide(new BigDecimal(totalNum.toString()), RoundingMode.HALF_UP));
        });
    }

    /**
     * 更新导出进度
     */
    public void updateProgress(Long taskId, Long count, Long totalNum) {
        execute(taskId, task -> {
            task.setProgress(new BigDecimal(count.toString())
                    .divide(new BigDecimal(totalNum.toString()), RoundingMode.HALF_UP));
        });
    }

    /**
     * 完成任务（成功）
     */
    public void finishTask(Long taskId, String result, String message) {
        execute(taskId, task -> {
            if (TaskStatusEnum.STOP.getStatus() != task.getStatus()) {
                task.setStatus(TaskStatusEnum.SUCCESS.getStatus());
            }
            task.setUrl(result);
            task.setMessage(message);
            task.setProgress(BigDecimal.ONE);
            task.setEndTime(LocalDateTime.now());
            task.setDuration(DateUtils.betweenMillis(task.getEndTime(), task.getStartTime()));
        });
    }

    /**
     * 完成任务（失败）
     */
    public void failTask(Long taskId, String message) {
        execute(taskId, task -> {
            task.setMessage(task.getMessage() + message);
            if (TaskStatusEnum.STOP.getStatus() != task.getStatus()) {
                task.setStatus(TaskStatusEnum.FAIL.getStatus());
            }
            task.setProgress(BigDecimal.ONE);
            if (Objects.isNull(task.getStartTime())) {
                task.setStartTime(LocalDateTime.now());
            }
            task.setEndTime(LocalDateTime.now());
            task.setDuration(DateUtils.betweenMillis(task.getEndTime(), task.getStartTime()));
        });
    }

    /**
     * 通用执行模板：查询 → 修改 → 保存
     */
    private void execute(Long taskId, Consumer<ExcelTaskTo> updater) {
        ExcelTaskTo task = remoteExcelTaskService.getExcelTaskById(taskId);
        if (Objects.isNull(task)) {
            log.warn("Task not found, taskId={}", taskId);
            return;
        }
        updater.accept(task);
        remoteExcelTaskService.updateExcelTaskById(task);
    }
}
