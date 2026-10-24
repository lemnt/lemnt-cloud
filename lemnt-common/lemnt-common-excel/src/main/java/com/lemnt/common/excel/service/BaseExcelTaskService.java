package com.lemnt.common.excel.service;

import com.lemnt.common.excel.domain.event.ExcelExportTaskEvent;
import com.lemnt.common.excel.domain.event.ExcelImportTaskEvent;
import com.lemnt.common.excel.domain.vo.ExcelImportResultVo;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * 表格任务
 *
 * @author wangxx
 */
public interface BaseExcelTaskService {

    /**
     * 同步导入
     *
     * @param file
     * @return
     */
    <E> void importExcel(MultipartFile file, Class<E> cls, Consumer<List<E>> callback) throws IOException;

    /**
     * 同步导出
     *
     * @param dataList
     * @param cls
     * @param fileName
     * @param response
     */
    <E> void excelExport(List<E> dataList, Class<E> cls, String fileName, HttpServletResponse response);

    /**
     * 添加导入任务
     *
     * @param file
     * @param event
     * @param tag
     * @return
     */
    void addImportTask(MultipartFile file, ExcelImportTaskEvent event, String tag);

    /**
     * 开始执行导入任务
     *
     * @param taskId
     */
    void startImportTask(Long taskId);

    /**
     * 上报导入进度
     *
     * @param id
     * @param count
     * @param totalNum
     */
    void uploadImportProgress(Long id, Integer count, Integer totalNum);

    /**
     * 完成导入任务
     *
     * @param result
     */
    void finishImportTask(ExcelImportResultVo result);

    /**
     * 导入任务执行失败
     *
     * @param taskId
     * @param message
     */
    void failImportTask(Long taskId, String message);

    /**
     * 添加导出任务
     *
     * @param event
     * @return
     */
    void addExportTask(ExcelExportTaskEvent event, String tag);

    /**
     * 开始执行导出任务
     *
     * @param taskId
     */
    void startExportTask(Long taskId);

    /**
     * 上报导出进度
     *
     * @param taskId
     * @param count
     * @param totalNum
     */
    void uploadExportProgress(Long taskId, Long count, Long totalNum);

    /**
     * 完成导出任务
     *
     * @param taskId
     * @param fileName
     */
    void finishExportTask(Long taskId, String fileName);

    /**
     * 导入任务执行失败
     *
     * @param taskId
     * @param message
     */
    void failExportTask(Long taskId, String message);

}
