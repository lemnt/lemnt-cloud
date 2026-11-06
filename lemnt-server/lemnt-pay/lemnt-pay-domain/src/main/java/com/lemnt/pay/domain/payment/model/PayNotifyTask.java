package com.lemnt.pay.domain.payment.model;

import java.time.LocalDateTime;

/**
 * 支付结果派发任务。
 */
public record PayNotifyTask(
    Long id,
    String type,
    String bizNo,
    String orderNo,
    String status,
    int notifyTimes,
    int maxNotifyTimes,
    LocalDateTime nextNotifyTime,
    LocalDateTime lastExecuteTime,
    String lastResponse,
    LocalDateTime createTime,
    Long executeVersion,
    LocalDateTime leaseUntil) {
    /**
     * 兼容未记录本批优惠与退款元数据的历史快照。
     */
    public PayNotifyTask(
        Long id,
        String type,
        String bizNo,
        String orderNo,
        String status,
        int notifyTimes,
        int maxNotifyTimes,
        LocalDateTime nextNotifyTime,
        LocalDateTime lastExecuteTime,
        String lastResponse,
        LocalDateTime createTime) {
        this(
            id,
            type,
            bizNo,
            orderNo,
            status,
            notifyTimes,
            maxNotifyTimes,
            nextNotifyTime,
            lastExecuteTime,
            lastResponse,
            createTime,
            null,
            null);
    }
}
