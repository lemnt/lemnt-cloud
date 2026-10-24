package com.lemnt.common.log.service;

import com.lemnt.common.log.event.OperLogEvent;
import com.lemnt.common.log.listener.producer.OperLogProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 异步调用日志服务
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class AsyncLogService {

    private final OperLogProducer operLogProducer;

    /**
     * 保存系统日志记录
     */
    @Async
    public void saveSysLog(OperLogEvent event) {
        operLogProducer.send(event);
    }
}
