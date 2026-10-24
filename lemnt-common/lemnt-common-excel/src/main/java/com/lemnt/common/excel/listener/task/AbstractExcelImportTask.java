package com.lemnt.common.excel.listener.task;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.lemnt.common.excel.converter.ExcelImportAnnotationConverter;
import com.lemnt.common.excel.domain.event.ExcelImportTaskEvent;
import com.lemnt.common.excel.domain.vo.ExcelImportResultVo;
import com.lemnt.common.excel.service.BaseExcelTaskService;
import com.lemnt.common.oss.service.OssFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * excel 表格导入处理器抽象基类
 *
 * @author wangxx
 */
@Slf4j
public abstract class AbstractExcelImportTask<V> implements BaseExcelImportTask {

    @Autowired
    private BaseExcelTaskService baseExcelTaskService;
    @Autowired
    private OssFileService ossFileService;

    /**
     * 导入excel的类型
     */
    private final Class<V> vClass;

    @SuppressWarnings("unchecked")
    public AbstractExcelImportTask() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.vClass = (Class<V>) type.getActualTypeArguments()[0];
    }

    /**
     * 分页插入数据
     *
     * @param dataList
     * @return
     */
    protected abstract Integer batchSave(List<V> dataList);

    @Override
    public ExcelImportResultVo execute(ExcelImportTaskEvent event) throws Exception {
        Map<Integer, Field> headMap = ExcelImportAnnotationConverter.buildHeadMap(this.vClass);
        AtomicInteger processed = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        StringBuilder message = new StringBuilder();
        ExcelImportResultVo result = new ExcelImportResultVo();
        result.setTaskId(event.getTaskId());
        try (InputStream fis = ossFileService.getFile(event.getFileUrl())) {
            EasyExcel.read(fis, new ReadListener<Map<Integer, Object>>() {
                private static final int BATCH_SIZE = 1000;
                private final List<V> dataList = ListUtils.newArrayListWithExpectedSize(1000);

                @Override
                public void invoke(Map<Integer, Object> data, AnalysisContext context) {
                    processed.incrementAndGet();
                    try {
                        V entity = ExcelImportAnnotationConverter.convertRow(headMap, vClass, data);
                        if (Objects.nonNull(entity)) {
                            dataList.add(entity);
                        }
                    } catch (Exception e) {
                        fail.incrementAndGet();
                        this.appendMsg("第 " + (context.readRowHolder().getRowIndex() + 1) + " 行解析失败：" + e.getMessage());
                    }
                    if (dataList.size() >= BATCH_SIZE) {
                        this.flush();
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    this.flush();
                }

                /**
                 * 处理处理结果
                 */
                private void flush() {
                    if (CollUtil.isEmpty(dataList)) {
                        return;
                    }
                    try {
                        int count = batchSave(dataList);
                        success.addAndGet(count);
                        fail.addAndGet(dataList.size() - count);
                    } catch (Exception e) {
                        fail.addAndGet(dataList.size());
                        appendMsg("批量保存失败：" + e.getMessage());
                    } finally {
                        baseExcelTaskService.uploadImportProgress(event.getTaskId(), processed.get(), success.get());
                        dataList.clear();
                    }
                }

                /**
                 * 处理异常消息
                 *
                 * @param msg
                 */
                private void appendMsg(String msg) {
                    if (message.length() > 0) {
                        message.append("；");
                    }
                    message.append(msg);
                }
            }).sheet().doRead();
        } catch (Exception e) {
            result.setMessage("导入失败：" + e.getMessage());
        }
        result.setTotalCount(processed.get());
        result.setSuccessCount(success.get());
        result.setFailCount(fail.get());
        result.setMessage(result.buildMessage(message.toString()));
        return result;
    }
}
