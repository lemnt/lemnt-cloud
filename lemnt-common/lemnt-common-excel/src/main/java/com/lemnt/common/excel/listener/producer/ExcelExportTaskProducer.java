package com.lemnt.common.excel.listener.producer;

import com.lemnt.common.excel.domain.event.ExcelExportTaskEvent;
import com.lemnt.common.mq.constant.MQConstants;
import com.lemnt.common.mq.producer.BaseProducer;
import org.springframework.stereotype.Component;

/**
 * 表格导出任务消息生产者
 *
 * @author wangxx
 */
@Component
public class ExcelExportTaskProducer extends BaseProducer<ExcelExportTaskEvent> {

    /**
     * 消息发送
     *
     * @param event
     * @param tag
     */
    public void send(ExcelExportTaskEvent event, String tag) {
        sendWithTag(MQConstants.Topic.EXCEL_EXPORT_TASK, tag, event);
    }
}
