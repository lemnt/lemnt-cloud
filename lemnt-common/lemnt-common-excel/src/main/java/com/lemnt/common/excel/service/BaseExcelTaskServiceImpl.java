package com.lemnt.common.excel.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.excel.api.RemoteExcelTaskService;
import com.lemnt.common.excel.converter.ExcelExportAnnotationConverter;
import com.lemnt.common.excel.converter.ExcelImportAnnotationConverter;
import com.lemnt.common.excel.domain.event.ExcelImportTaskEvent;
import com.lemnt.common.excel.domain.event.ExcelExportTaskEvent;
import com.lemnt.common.excel.domain.vo.ExcelImportResultVo;
import com.lemnt.common.excel.handler.ExcelColumnWidthHandler;
import com.lemnt.common.excel.handler.ExcelStyleHandler;
import com.lemnt.common.oss.service.OssFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * 表格任务
 *
 * @author wangxx
 */
@Slf4j
@Component
public class BaseExcelTaskServiceImpl implements BaseExcelTaskService {

    @DubboReference
    private RemoteExcelTaskService remoteExcelTaskService;
    private final OssFileService ossFileService;
    private final TaskLifecycleTemplate taskLifecycle;

    public BaseExcelTaskServiceImpl(OssFileService ossFileService, TaskLifecycleTemplate taskLifecycle) {
        this.ossFileService = ossFileService;
        this.taskLifecycle = taskLifecycle;
    }

    @Override
    public <E> void importExcel(MultipartFile file, Class<E> cls, Consumer<List<E>> callback) throws IOException {
        Map<Integer, Field> headMap = ExcelImportAnnotationConverter.buildHeadMap(cls);
        try (InputStream fis = file.getInputStream()) {
            EasyExcel.read(fis, new ReadListener<Map<Integer, Object>>() {
                private final List<E> dataList = ListUtils.newArrayListWithExpectedSize(1000);
                final int batchSize = 1000;

                @Override
                public void invoke(Map<Integer, Object> data, AnalysisContext context) {
                    E entity = ExcelImportAnnotationConverter.convertRow(headMap, cls, data);
                    if (Objects.isNull(entity)) {
                        return;
                    }
                    dataList.add(entity);
                    if (dataList.size() >= batchSize) {
                        callback.accept(new ArrayList<>(dataList));
                        dataList.clear();
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    if (!dataList.isEmpty()) {
                        callback.accept(dataList);
                        dataList.clear();
                    }
                }
            }).sheet().doRead();
        }
    }

    @Override
    public <E> void excelExport(List<E> dataList, Class<E> cls, String fileName, HttpServletResponse response) {
        try {
            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            response.setHeader("Content-Disposition",
                    "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
            ExcelStyleHandler styleHandler = new ExcelStyleHandler(cls);
            ExcelColumnWidthHandler widthHandler = new ExcelColumnWidthHandler(cls);
            EasyExcel.write(response.getOutputStream())
                    .head(ExcelExportAnnotationConverter.buildHead(cls))
                    .registerWriteHandler(styleHandler)
                    .registerWriteHandler(widthHandler)
                    .sheet().doWrite(ExcelExportAnnotationConverter.convertRows(dataList, cls));
            response.flushBuffer();
        } catch (IOException e) {
            log.error("Excel导出失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void addImportTask(MultipartFile file, ExcelImportTaskEvent event, String tag) {
        String fileUrl;
        try {
            fileUrl = ossFileService.uploadFile(file);
        } catch (Exception e) {
            log.error("Excel导入失败: {}", e.getMessage(), e);
            throw new ServiceException("文件导入失败");
        }
        if (StringUtils.isEmpty(fileUrl)) {
            throw new ServiceException("文件导入失败");
        }
        event.setFileUrl(fileUrl);
        remoteExcelTaskService.addImportTask(event, tag);
    }

    @Override
    public void startImportTask(Long taskId) {
        taskLifecycle.startTask(taskId);
    }

    @Override
    public void uploadImportProgress(Long taskId, Integer count, Integer totalNum) {
        taskLifecycle.updateProgress(taskId, count, totalNum);
    }

    @Override
    public void finishImportTask(ExcelImportResultVo result) {
        taskLifecycle.finishTask(result.getTaskId(), null, result.getMessage());
    }

    @Override
    public void failImportTask(Long taskId, String message) {
        taskLifecycle.failTask(taskId, message);
    }

    @Override
    public void addExportTask(ExcelExportTaskEvent event, String tag) {
        remoteExcelTaskService.addExportTask(event, tag);
    }

    @Override
    public void startExportTask(Long taskId) {
        taskLifecycle.startTask(taskId);
    }

    @Override
    public void uploadExportProgress(Long taskId, Long count, Long totalNum) {
        taskLifecycle.updateProgress(taskId, count, totalNum);
    }

    @Override
    public void finishExportTask(Long taskId, String fileName) {
        taskLifecycle.finishTask(taskId, fileName, null);
    }

    @Override
    public void failExportTask(Long taskId, String message) {
        taskLifecycle.failTask(taskId, message);
    }
}
