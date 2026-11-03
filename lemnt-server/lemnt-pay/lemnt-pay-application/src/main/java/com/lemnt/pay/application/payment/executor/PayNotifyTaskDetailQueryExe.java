package com.lemnt.pay.application.payment.executor;

import com.lemnt.pay.application.payment.converter.PayNotifyDTOConverter;
import com.lemnt.pay.application.payment.dto.PayNotifyTaskDetailDTO;
import com.lemnt.pay.domain.payment.gateway.PayNotifyTaskGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 派发任务详情与日志查询。 */
@Component
@RequiredArgsConstructor
public class PayNotifyTaskDetailQueryExe {
    private final PayNotifyTaskGateway tasks;

    public PayNotifyTaskDetailDTO execute(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("任务编号不合法");
        }
        var task = tasks.find(id);
        return task == null
                ? null
                : new PayNotifyTaskDetailDTO(
                        PayNotifyDTOConverter.task(task),
                        tasks.logs(id).stream().map(PayNotifyDTOConverter::log).toList());
    }
}
