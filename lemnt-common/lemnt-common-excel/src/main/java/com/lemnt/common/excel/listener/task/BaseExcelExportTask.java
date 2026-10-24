package com.lemnt.common.excel.listener.task;

import com.lemnt.common.excel.domain.event.ExcelExportTaskEvent;

/**
 * 表格导出消费者
 *
 * @author wangxx
 */
public interface BaseExcelExportTask {

    /**
     * 策略标识
     */
    String getType();

    /**
     * 执行导出
     *
     * @param event
     * @return
     * @throws Exception
     */
    String execute(ExcelExportTaskEvent event) throws Exception;
}
