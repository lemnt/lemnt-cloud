package com.lemnt.common.excel.factory;

import com.lemnt.common.excel.listener.task.BaseExcelImportTask;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表格导入工厂
 *
 * @author wangxx
 */
@Component
public class ExcelImportTaskFactory implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final Map<String, BaseExcelImportTask> taskMap = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, BaseExcelImportTask> beans = applicationContext.getBeansOfType(BaseExcelImportTask.class);
        for (BaseExcelImportTask task : beans.values()) {
            String type = task.getType();
            if (type == null || type.isEmpty()) {
                throw new IllegalArgumentException("BaseExcelExportTask must implement getType(): " + task.getClass());
            }
            taskMap.put(type, task);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取 Handler，未找到抛出异常
     */
    public BaseExcelImportTask getTask(String type) {
        BaseExcelImportTask task = taskMap.get(type);
        if (task == null) {
            throw new IllegalArgumentException("No ExcelExportHandler found for type: " + type);
        }
        return task;
    }
}
