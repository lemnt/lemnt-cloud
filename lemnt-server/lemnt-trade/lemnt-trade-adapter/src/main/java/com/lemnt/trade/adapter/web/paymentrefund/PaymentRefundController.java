package com.lemnt.trade.adapter.web.paymentrefund;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.paymentrefund.converter.PaymentRefundReqConverter;
import com.lemnt.trade.adapter.web.paymentrefund.converter.PaymentRefundResConverter;
import com.lemnt.trade.adapter.web.paymentrefund.req.PaymentRefundPageReq;
import com.lemnt.trade.adapter.web.paymentrefund.res.PaymentRefundDetailRes;
import com.lemnt.trade.adapter.web.paymentrefund.res.PaymentRefundPageRes;
import com.lemnt.trade.application.paymentrefund.executor.PaymentRefundDetailQueryExe;
import com.lemnt.trade.application.paymentrefund.executor.PaymentRefundPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 后台支付退款交易管理接口。 */
@Tag(name = "支付退单管理")
@RestController
@RequestMapping("/trade/admin/payment-refunds")
@RequiredArgsConstructor
public class PaymentRefundController {
    private final PaymentRefundPageQueryExe pageQuery;
    private final PaymentRefundDetailQueryExe detailQuery;

    /** 分页查询本地交易记录。 */
    @GetMapping
    @Operation(summary = "分页查询支付退款交易")
    @RequiresPermissions("trade:payment-refund:list")
    public R<PaymentRefundPageRes> page(@Valid @ModelAttribute PaymentRefundPageReq req) {
        var page = pageQuery.execute(PaymentRefundReqConverter.page(req));
        return R.page(page.getRecords().stream().map(PaymentRefundResConverter::page).toList(), page.getTotal());
    }

    /** 查询本地交易详情。 */
    @GetMapping("/{no}")
    @Operation(summary = "查询支付退款交易详情")
    @RequiresPermissions("trade:payment-refund:query")
    public R<PaymentRefundDetailRes> detail(@PathVariable String no) {
        var value = detailQuery.execute(no);
        return value == null ? R.fail("交易记录不存在") : R.ok(PaymentRefundResConverter.detail(value));
    }
}
