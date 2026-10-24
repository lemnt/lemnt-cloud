package com.lemnt.common.excel.listener.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.crypto.digest.DigestUtil;
import com.lemnt.common.core.utils.JsonUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.lemnt.common.excel.converter.ExcelExportAnnotationConverter;
import com.lemnt.common.excel.domain.vo.ExportListVo;
import com.lemnt.common.excel.domain.event.ExcelExportTaskEvent;
import com.lemnt.common.excel.handler.ExcelColumnWidthHandler;
import com.lemnt.common.excel.handler.ExcelStyleHandler;
import com.lemnt.common.excel.service.BaseExcelTaskService;
import com.lemnt.common.oss.constant.OssFileTypeEnum;
import com.lemnt.common.oss.service.OssFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.lang.reflect.ParameterizedType;

/**
 * excel 表格导出处理器抽象基类
 *
 * @author wangxx
 */
@Slf4j
public abstract class AbstractExcelExportTask<Q, V> implements BaseExcelExportTask {

    @Autowired
    private BaseExcelTaskService baseExcelTaskService;
    @Autowired
    private OssFileService ossFileService;
    @Value("${upload.file-path}")
    private String filePath;
    /**
     * 默认每页处理的大小
     */
    private static final int DEFAULT_PAGE_SIZE = 1000;

    /**
     * 参数的类型
     */
    private final Class<Q> qClass;

    /**
     * 导出excel的类型
     */
    private final Class<V> vClass;

    @SuppressWarnings("unchecked")
    public AbstractExcelExportTask() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.qClass = (Class<Q>) type.getActualTypeArguments()[0];
        this.vClass = (Class<V>) type.getActualTypeArguments()[1];
    }

    /**
     * 分页查询数据
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    protected abstract ExportListVo<V> pageExport(int pageNum, int pageSize, Q params);

    /**
     * 分页大小
     *
     * @return
     */
    protected int pageSize() {
        return DEFAULT_PAGE_SIZE;
    }

    @Override
    public String execute(ExcelExportTaskEvent event) throws Exception {
        Q params = JsonUtils.toBean(event.getParams(), this.qClass);
        String fileName = DigestUtil.md5Hex(event.getTaskId() + event.getType() + System.currentTimeMillis()) + StrPool.DOT + OssFileTypeEnum.XLSX.getSuffix();
        String exportFilePath = filePath + StrPool.SLASH + "export" + StrPool.SLASH + fileName;
        File exportFile = new File(exportFilePath);
        FileUtil.mkParentDirs(exportFile);
        ExcelStyleHandler styleHandler = new ExcelStyleHandler(this.vClass);
        ExcelColumnWidthHandler widthHandler = new ExcelColumnWidthHandler(this.vClass);
        try (ExcelWriter writer = EasyExcel.write(exportFile)
                .head(ExcelExportAnnotationConverter.buildHead(this.vClass))
                .registerWriteHandler(styleHandler)
                .registerWriteHandler(widthHandler).build()) {
            WriteSheet sheet = EasyExcel.writerSheet().build();
            long count = 0, totalNum = 0;
            int pageNumber = 1;
            while (true) {
                ExportListVo<V> data = this.pageExport(pageNumber, this.pageSize(), params);
                if (CollUtil.isEmpty(data.getList())) {
                    break;
                }
                writer.write(ExcelExportAnnotationConverter.convertRows(data.getList(), this.vClass), sheet);
                if (data.getList().size() < this.pageSize()) {
                    break;
                }
                if (totalNum == 0) {
                    totalNum = data.getTotal();
                }
                count += data.getList().size();
                pageNumber++;
                // 进行进度上报
                baseExcelTaskService.uploadExportProgress(event.getTaskId(), count, totalNum);
            }
            // 追加总计
            styleHandler.appendStatisticsRow();
        }
        try (InputStream is = new FileInputStream(exportFile)) {
            return ossFileService.uploadStream(is, fileName, OssFileTypeEnum.XLSX.getContentType(), exportFile.length());
        } finally {
            FileUtil.del(exportFile);
        }
    }
}
