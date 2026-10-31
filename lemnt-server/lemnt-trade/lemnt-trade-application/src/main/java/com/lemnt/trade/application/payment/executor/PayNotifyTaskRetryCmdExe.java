package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.service.PayNotifyTaskService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 后台人工重发派发任务。 */
@Component
@RequiredArgsConstructor
public class PayNotifyTaskRetryCmdExe {
    private final PayNotifyTaskService service;

    /** 重置并立即尝试；本次仍失败时返回 false，任务保留失败日志。 */
    public boolean execute(Long id) {
        return service.retry(id);
    }
}
