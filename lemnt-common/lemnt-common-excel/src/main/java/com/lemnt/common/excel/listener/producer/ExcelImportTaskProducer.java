package com.lemnt.common.excel.listener.producer;

import com.lemnt.common.excel.domain.event.ExcelImportTaskEvent;
import com.lemnt.common.mq.constant.MQConstants;
import com.lemnt.common.mq.producer.BaseProducer;
import org.springframework.stereotype.Component;

/**
 * 表格导入任务消息生产者
 *
 * @author wangxx
 */
@Component
public class ExcelImportTaskProducer extends BaseProducer<ExcelImportTaskEvent> {

    /**
     * 消息发送
     *
     * @param event
     * @param tag
     */
    public void send(ExcelImportTaskEvent event, String tag) {
        sendWithTag(MQConstants.Topic.EXCEL_IMPORT_TASK, tag, event);
    }
}
