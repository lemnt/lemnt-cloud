package com.lemnt.trade.adapter.web.paynotify;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.paynotify.converter.PayNotifyResConverter;
import com.lemnt.trade.adapter.web.paynotify.req.PayNotifyTaskPageReq;
import com.lemnt.trade.adapter.web.paynotify.res.PayNotifyTaskDetailRes;
import com.lemnt.trade.adapter.web.paynotify.res.PayNotifyTaskRes;
import com.lemnt.trade.application.payment.executor.PayNotifyTaskDetailQueryExe;
import com.lemnt.trade.application.payment.executor.PayNotifyTaskPageQueryExe;
import com.lemnt.trade.application.payment.executor.PayNotifyTaskRetryCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 支付结果派发任务管理接口。 */
@Tag(name = "支付派发任务")
@RestController
@RequestMapping("/trade/admin/pay-notify-tasks")
@RequiredArgsConstructor
public class PayNotifyTaskController {
    private final PayNotifyTaskPageQueryExe page;
    private final PayNotifyTaskDetailQueryExe detail;
    private final PayNotifyTaskRetryCmdExe retry;

    /** 派发任务分页。 */
    @GetMapping
    @Operation(summary = "派发任务分页")
    @RequiresPermissions("trade:pay-notify:list")
    public R<PayNotifyTaskRes> page(@Valid @ModelAttribute PayNotifyTaskPageReq req) {
        var value = page.execute(PayNotifyResConverter.page(req));
        return R.page(value.getRecords().stream().map(PayNotifyResConverter::task).toList(), value.getTotal());
    }

    /** 派发任务详情与尝试日志。 */
    @GetMapping("/{id}")
    @Operation(summary = "派发任务详情")
    @RequiresPermissions("trade:pay-notify:query")
    public R<PayNotifyTaskDetailRes> detail(@PathVariable Long id) {
        var value = detail.execute(id);
        return value == null ? R.fail("派发任务不存在") : R.ok(PayNotifyResConverter.detail(value));
    }

    /** 人工重发失败任务；本次仍失败时返回错误并保留日志。 */
    @PostMapping("/{id}/retry")
    @Operation(summary = "重发派发任务")
    @RequiresPermissions("trade:pay-notify:retry")
    public R<Boolean> retry(@PathVariable Long id) {
        if (!retry.execute(id)) {
            throw new IllegalStateException("重发失败，已记录日志");
        }
        return R.ok(true);
    }
}
