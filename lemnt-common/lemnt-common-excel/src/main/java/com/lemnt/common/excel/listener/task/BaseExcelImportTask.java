package com.lemnt.common.excel.listener.task;

import com.lemnt.common.excel.domain.event.ExcelImportTaskEvent;
import com.lemnt.common.excel.domain.vo.ExcelImportResultVo;

/**
 * 表格导入消费者
 *
 * @author wangxx
 */
public interface BaseExcelImportTask {

    /**
     * 策略标识
     */
    String getType();

    /**
     * 执行导入
     *
     * @param event
     * @return
     * @throws Exception
     */
    ExcelImportResultVo execute(ExcelImportTaskEvent event) throws Exception;
}
