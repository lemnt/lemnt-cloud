package com.lemnt.trade.infrastructure.payment.converter;

import com.lemnt.trade.domain.payment.model.PayNotifyLog;
import com.lemnt.trade.domain.payment.model.PayNotifyTask;
import com.lemnt.trade.infrastructure.payment.dataobject.PayNotifyLogDO;
import com.lemnt.trade.infrastructure.payment.dataobject.PayNotifyTaskDO;

/** 派发任务与日志表字段转换。 */
public final class PayNotifyTaskConverter {
    private PayNotifyTaskConverter() {
    }

    public static PayNotifyTask toTask(PayNotifyTaskDO row) {
        return row == null
                ? null
                : new PayNotifyTask(row.getId(), row.getType(), row.getBizNo(), row.getOrderNo(), row.getStatus(),
                        row.getNotifyTimes() == null ? 0 : row.getNotifyTimes(),
                        row.getMaxNotifyTimes() == null ? 0 : row.getMaxNotifyTimes(),
                        row.getNextNotifyTime(), row.getLastExecuteTime(), row.getLastResponse(),
                        row.getCreateTime());
    }

    public static PayNotifyTaskDO toDO(PayNotifyTask value) {
        var row = new PayNotifyTaskDO();
        row.setId(value.id());
        row.setType(value.type());
        row.setBizNo(value.bizNo());
        row.setOrderNo(value.orderNo());
        row.setStatus(value.status());
        row.setNotifyTimes(value.notifyTimes());
        row.setMaxNotifyTimes(value.maxNotifyTimes());
        row.setNextNotifyTime(value.nextNotifyTime());
        row.setLastExecuteTime(value.lastExecuteTime());
        row.setLastResponse(value.lastResponse());
        return row;
    }

    public static PayNotifyLog toLog(PayNotifyLogDO row) {
        return row == null
                ? null
                : new PayNotifyLog(row.getId(), row.getTaskId(),
                        row.getNotifyTimes() == null ? 0 : row.getNotifyTimes(), row.getStatus(),
                        row.getResponse(), row.getCreateTime());
    }

    public static PayNotifyLogDO toDO(PayNotifyLog value) {
        var row = new PayNotifyLogDO();
        row.setTaskId(value.taskId());
        row.setNotifyTimes(value.notifyTimes());
        row.setStatus(value.status());
        row.setResponse(value.response());
        return row;
    }
}
