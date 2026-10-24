package com.lemnt.common.excel.api;

import com.lemnt.common.excel.domain.event.ExcelImportTaskEvent;
import com.lemnt.common.excel.domain.to.ExcelTaskTo;
import com.lemnt.common.excel.domain.event.ExcelExportTaskEvent;

/**
 * 表格任务远程接口
 *
 * @author wangxx
 */
public interface RemoteExcelTaskService {

    /**
     * 添加导出任务
     *
     * @param event
     * @return
     */
    void addExportTask(ExcelExportTaskEvent event, String tag);

    /**
     * 添加导入任务
     *
     * @param event
     * @param tag
     */
    void addImportTask(ExcelImportTaskEvent event, String tag);

    /**
     * 根据ID获取任务对象
     *
     * @param taskId
     * @return ExportTask
     */
    ExcelTaskTo getExcelTaskById(Long taskId);

    /**
     * 修改导出任务
     *
     * @param task
     * @return Boolean
     */
    void updateExcelTaskById(ExcelTaskTo task);
}
