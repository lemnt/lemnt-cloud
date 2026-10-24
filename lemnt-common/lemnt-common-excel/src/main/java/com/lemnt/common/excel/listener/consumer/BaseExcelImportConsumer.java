package com.lemnt.common.excel.listener.consumer;

import com.lemnt.common.excel.domain.event.ExcelImportTaskEvent;
import com.lemnt.common.excel.domain.vo.ExcelImportResultVo;
import com.lemnt.common.excel.factory.ExcelImportTaskFactory;
import com.lemnt.common.excel.listener.task.BaseExcelImportTask;
import com.lemnt.common.excel.service.BaseExcelTaskService;
import com.lemnt.common.mq.consumer.BaseConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基础表格导入消费者
 *
 * @author wangxx
 */
@Slf4j
public class BaseExcelImportConsumer<T extends ExcelImportTaskEvent> extends BaseConsumer<T> {

    @Autowired
    private ExcelImportTaskFactory excelImportTaskFactory;
    @Autowired
    private BaseExcelTaskService baseExcelTaskService;

    @Override
    protected void handle(T message) {
        try {
            log.info("开始执行 {}", message.getType());
            baseExcelTaskService.startImportTask(message.getTaskId());
            BaseExcelImportTask excelImportTask = excelImportTaskFactory.getTask(message.getType());
            ExcelImportResultVo result = excelImportTask.execute(message);
            baseExcelTaskService.finishImportTask(result);
            log.info("完成执行 {}", message.getType());
        } catch (Exception e) {
            log.error("任务 {} 执行失败", message.getType(), e);
            baseExcelTaskService.failImportTask(message.getTaskId(), e.getMessage());
        }
    }
}
