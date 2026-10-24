package com.lemnt.common.excel.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表格导出任务常量
 *
 * @author wangxx
 */
@Getter
public enum ExcelExportTaskEnum {

    SYSTEM_USER_EXPORT("userExcelExportTask", "用户导出"),
    ;

    private final String beanName;

    private final String desc;

    ExcelExportTaskEnum(String beanName, String desc) {
        this.beanName = beanName;
        this.desc = desc;
    }

    private static final Map<String, ExcelExportTaskEnum> TYPE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(
                            ExcelExportTaskEnum::getBeanName,
                            Function.identity()
                    ));

    /**
     * 根据 beanName 获取枚举
     */
    public static ExcelExportTaskEnum of(String beanName) {
        ExcelExportTaskEnum taskEnum = TYPE_MAP.get(beanName);
        if (taskEnum == null) {
            throw new IllegalArgumentException("未知的 Excel 导出任务类型: " + beanName);
        }
        return taskEnum;
    }
}
