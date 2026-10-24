package com.lemnt.system.adapter.mq.log;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.common.log.event.OperLogEvent;
import com.lemnt.common.mq.constant.MQConstants;
import com.lemnt.system.application.log.cmd.OperLogAddCmd;
import com.lemnt.system.application.log.executor.OperLogAddCmdExe;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 接收操作审计事件，通过应用层执行器写入操作日志。
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = MQConstants.Topic.OPER_LOG_EVENT,
        consumerGroup = MQConstants.Group.SYSTEM_OPER_LOG_EVENT)
public class OperLogConsumer implements RocketMQListener<String> {

    private final OperLogAddCmdExe operLogAddCmdExe;

    /**
     * 保留事件发生时间；入库失败向消息容器抛错，由 RocketMQ 重试。
     */
    @Override
    public void onMessage(String message) {
        OperLogEvent event = JsonUtils.toBean(message, OperLogEvent.class);
        if (event == null) {
            throw new IllegalArgumentException("操作审计事件不能为空");
        }
        OperLogAddCmd cmd = new OperLogAddCmd(event.getTitle(), event.getBusinessType(),
                event.getMethod(), event.getRequestMethod(), event.getOperatorType(),
                event.getOperName(), event.getDeptName(), event.getOperUrl(), event.getOperIp(),
                null, event.getOperParam(), event.getJsonResult(), event.getStatus(),
                event.getErrorMsg(), event.getOperTime(), event.getCostTime());
        if (!Boolean.TRUE.equals(operLogAddCmdExe.execute(cmd))) {
            throw new IllegalStateException("操作审计日志写入失败");
        }
    }
}
