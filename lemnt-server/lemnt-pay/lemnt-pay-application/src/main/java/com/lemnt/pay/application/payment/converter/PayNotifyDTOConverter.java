package com.lemnt.pay.application.payment.converter;

import com.lemnt.pay.application.payment.dto.PayNotifyLogDTO;
import com.lemnt.pay.application.payment.dto.PayNotifyTaskDTO;
import com.lemnt.pay.domain.payment.model.PayNotifyLog;
import com.lemnt.pay.domain.payment.model.PayNotifyTask;

/**
 * 派发任务与日志应用输出转换。
 */
public final class PayNotifyDTOConverter {
    private PayNotifyDTOConverter() {
    }

    public static PayNotifyTaskDTO task(PayNotifyTask value) {
        return value == null
            ? null
            : new PayNotifyTaskDTO(
            value.id(),
            value.type(),
            value.bizNo(),
            value.orderNo(),
            value.status(),
            value.notifyTimes(),
            value.maxNotifyTimes(),
            value.nextNotifyTime(),
            value.lastExecuteTime(),
            value.lastResponse(),
            value.createTime());
    }

    public static PayNotifyLogDTO log(PayNotifyLog value) {
        return value == null
            ? null
            : new PayNotifyLogDTO(
            value.notifyTimes(), value.status(), value.response(), value.createTime());
    }
}
