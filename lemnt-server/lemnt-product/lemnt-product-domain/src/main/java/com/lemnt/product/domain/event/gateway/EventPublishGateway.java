package com.lemnt.product.domain.event.gateway;

import com.lemnt.product.domain.event.model.OutboxMessage;

/** 消息传输边界，不在数据库事务中等待消息服务。 */
public interface EventPublishGateway {
    /** 同步确认broker接受；异常必须传回恢复任务。 */
    void publish(OutboxMessage message);
}
