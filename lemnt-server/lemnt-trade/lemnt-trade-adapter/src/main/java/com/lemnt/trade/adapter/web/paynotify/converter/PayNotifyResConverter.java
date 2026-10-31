package com.lemnt.trade.adapter.web.paynotify.converter;

import com.lemnt.trade.adapter.web.paynotify.req.PayNotifyTaskPageReq;
import com.lemnt.trade.adapter.web.paynotify.res.PayNotifyLogRes;
import com.lemnt.trade.adapter.web.paynotify.res.PayNotifyTaskDetailRes;
import com.lemnt.trade.adapter.web.paynotify.res.PayNotifyTaskRes;
import com.lemnt.trade.application.payment.dto.PayNotifyLogDTO;
import com.lemnt.trade.application.payment.dto.PayNotifyTaskDTO;
import com.lemnt.trade.application.payment.dto.PayNotifyTaskDetailDTO;
import com.lemnt.trade.application.payment.qry.PayNotifyTaskPageQry;

/** 派发任务请求与响应转换。 */
public final class PayNotifyResConverter {
    private PayNotifyResConverter() {
    }

    public static PayNotifyTaskPageQry page(PayNotifyTaskPageReq req) {
        return new PayNotifyTaskPageQry(req.type(), req.bizNo(), req.orderNo(), req.status(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }

    public static PayNotifyTaskRes task(PayNotifyTaskDTO value) {
        return value == null
                ? null
                : new PayNotifyTaskRes(value.id(), value.type(), value.bizNo(), value.orderNo(), value.status(),
                        value.notifyTimes(), value.maxNotifyTimes(), value.nextNotifyTime(), value.lastExecuteTime(),
                        value.lastResponse(), value.createTime());
    }

    public static PayNotifyLogRes log(PayNotifyLogDTO value) {
        return value == null
                ? null
                : new PayNotifyLogRes(value.notifyTimes(), value.status(), value.response(), value.createTime());
    }

    public static PayNotifyTaskDetailRes detail(PayNotifyTaskDetailDTO value) {
        return value == null
                ? null
                : new PayNotifyTaskDetailRes(task(value.task()),
                        value.logs().stream().map(PayNotifyResConverter::log).toList());
    }
}
