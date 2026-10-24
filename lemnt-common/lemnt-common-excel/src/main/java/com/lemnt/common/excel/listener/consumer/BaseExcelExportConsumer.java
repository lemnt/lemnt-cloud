package com.lemnt.common.excel.listener.consumer;

import com.lemnt.common.excel.domain.event.ExcelExportTaskEvent;
import com.lemnt.common.excel.factory.ExcelExportTaskFactory;
import com.lemnt.common.excel.listener.task.BaseExcelExportTask;
import com.lemnt.common.excel.service.BaseExcelTaskService;
import com.lemnt.common.mq.consumer.BaseConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基础表格导出消费者
 *
 * @author wangxx
 */
@Slf4j
public class BaseExcelExportConsumer<T extends ExcelExportTaskEvent> extends BaseConsumer<T> {

    @Autowired
    private ExcelExportTaskFactory excelExportTaskFactory;
    @Autowired
    private BaseExcelTaskService baseExcelTaskService;

    @Override
    protected void handle(T message) {
        try {
            log.info("开始执行 {}", message.getType());
            baseExcelTaskService.startExportTask(message.getTaskId());
            BaseExcelExportTask excelExportTask = excelExportTaskFactory.getTask(message.getType());
            String fileName = excelExportTask.execute(message);
            baseExcelTaskService.finishExportTask(message.getTaskId(), fileName);
            log.info("完成执行 {}", message.getType());
        } catch (Exception e) {
            log.error("任务 {} 执行失败", message.getType(), e);
            baseExcelTaskService.failExportTask(message.getTaskId(), e.getMessage());
        }
    }
}
