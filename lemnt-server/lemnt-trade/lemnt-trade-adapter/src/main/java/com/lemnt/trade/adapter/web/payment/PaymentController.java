package com.lemnt.trade.adapter.web.payment;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.payment.converter.PaymentReqConverter;
import com.lemnt.trade.adapter.web.payment.converter.PaymentResConverter;
import com.lemnt.trade.adapter.web.payment.req.PaymentPageReq;
import com.lemnt.trade.adapter.web.payment.res.PaymentDetailRes;
import com.lemnt.trade.adapter.web.payment.res.PaymentPageRes;
import com.lemnt.trade.application.payment.executor.PaymentDetailQueryExe;
import com.lemnt.trade.application.payment.executor.PaymentPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 后台支付单管理接口。 */
@Tag(name = "支付单管理")
@RestController
@RequestMapping("/trade/admin/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentPageQueryExe pageQuery;
    private final PaymentDetailQueryExe detailQuery;

    /** 分页查询本地交易记录。 */
    @GetMapping
    @Operation(summary = "分页查询支付单")
    @RequiresPermissions("trade:payment:list")
    public R<PaymentPageRes> page(@Valid @ModelAttribute PaymentPageReq req) {
        var page = pageQuery.execute(PaymentReqConverter.page(req));
        return R.page(page.getRecords().stream().map(PaymentResConverter::page).toList(), page.getTotal());
    }

    /** 查询本地交易详情。 */
    @GetMapping("/{no}")
    @Operation(summary = "查询支付单详情")
    @RequiresPermissions("trade:payment:query")
    public R<PaymentDetailRes> detail(@PathVariable String no) {
        var value = detailQuery.execute(no);
        return value == null ? R.fail("交易记录不存在") : R.ok(PaymentResConverter.detail(value));
    }
}
