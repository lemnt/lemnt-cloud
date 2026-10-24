package com.lemnt.common.excel.domain.vo;

import com.lemnt.common.core.utils.StringUtils;
import lombok.Data;

/**
 * 导入结果
 *
 * @author wangxx
 */
@Data
public class ExcelImportResultVo {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 总处理条数
     */
    private int totalCount;

    /**
     * 成功条数
     */
    private int successCount;

    /**
     * 失败条数
     */
    private int failCount;

    /**
     * 结果描述信息（前端直接展示）
     */
    private String message;

    /**
     * 快速构建 message
     */
    public String buildMessage(String msg) {
        if (StringUtils.isNotBlank(msg)) {
            return String.format("导入完成：共处理 %d 条，成功 %d 条，失败 %d 条。原因：%s", totalCount, successCount, failCount, message);
        }
        return String.format("导入完成：共处理 %d 条，成功 %d 条，失败 %d 条", totalCount, successCount, failCount);
    }
}
