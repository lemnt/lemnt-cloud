package com.lemnt.order.domain.event.gateway;

import com.lemnt.order.domain.event.model.OutboxMessage;

import java.util.List;

/**
 * 本库事务消息；登记必须与业务写入同事务，发送在事务外。
 */
public interface MessageOutboxGateway {
    boolean replay(String eventType, String businessKey);

    /**
     * 同事件类型及业务键只登记一次；不同冻结内容拒绝覆盖。
     */
    void enqueue(String eventType, String businessKey, String aggregateNo, String payload);

    /**
     * 用数据库时钟领取短租约。
     */
    List<OutboxMessage> claim(int size);

    /**
     * 只有原租约持有者可以完成或重试。
     */
    boolean sent(OutboxMessage message);

    void failed(OutboxMessage message, String error);

    /**
     * 人工重试保持原事件号，同时隔离仍在执行的旧线程。
     */
    boolean retryFailed(String eventNo);
}
