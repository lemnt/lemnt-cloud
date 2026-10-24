package com.lemnt.common.log.listener.producer;

import com.lemnt.common.log.event.OperLogEvent;
import com.lemnt.common.mq.constant.MQConstants;
import com.lemnt.common.mq.producer.BaseProducer;
import org.springframework.stereotype.Component;

/**
 * 广告创意召回消息
 *
 * @author wangxx
 **/
@Component
public class OperLogProducer extends BaseProducer<OperLogEvent> {

    /**
     * 消息发送
     *
     * @param event
     */
    public void send(OperLogEvent event) {
        sendSync(MQConstants.Topic.OPER_LOG_EVENT, event);
    }

}
