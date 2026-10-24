package com.lemnt.common.excel.domain.event;

import lombok.Data;

import java.io.Serializable;

/**
 * 表格导出消息事件
 *
 * @author wangxx
 */
@Data
public class ExcelImportTaskEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务 Id
     */
    private Long taskId;

    /**
     * 任务 Bean
     */
    private String type;

    /**
     * 文件 URL
     */
    private String fileUrl;
}
