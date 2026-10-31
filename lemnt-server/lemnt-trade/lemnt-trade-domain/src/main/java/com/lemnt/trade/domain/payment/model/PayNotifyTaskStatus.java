package com.lemnt.trade.domain.payment.model;

/** 派发任务状态：PENDING 待派发、SUCCESS 成功、FAILED 失败待重试。 */
public enum PayNotifyTaskStatus {
    PENDING,
    SUCCESS,
    FAILED
}
