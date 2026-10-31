package com.lemnt.trade.adapter.web.recharge;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.recharge.converter.RechargeResConverter;
import com.lemnt.trade.adapter.web.recharge.req.WalletRechargeConfirmReq;
import com.lemnt.trade.adapter.web.recharge.req.WalletRechargePageReq;
import com.lemnt.trade.adapter.web.recharge.res.WalletRechargeRes;
import com.lemnt.trade.application.payment.executor.WalletRechargeConfirmCmdExe;
import com.lemnt.trade.application.payment.executor.WalletRechargeDetailQueryExe;
import com.lemnt.trade.application.payment.executor.WalletRechargePageQueryExe;
import com.lemnt.trade.application.payment.executor.WalletRechargeRefundCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 后台充值单管理；确认到账代替真实渠道回调。 */
@Tag(name = "充值单")
@RestController
@RequestMapping("/trade/admin/wallet-recharges")
@RequiredArgsConstructor
public class WalletRechargeController {
    private final WalletRechargePageQueryExe page;
    private final WalletRechargeDetailQueryExe detail;
    private final WalletRechargeConfirmCmdExe confirm;
    private final WalletRechargeRefundCmdExe refund;

    @GetMapping
    @Operation(summary = "充值单分页")
    @RequiresPermissions("trade:wallet-recharge:list")
    public R<WalletRechargeRes> page(@Valid @ModelAttribute WalletRechargePageReq req) {
        var value = page.execute(RechargeResConverter.rechargePage(req));
        return R.page(value.getRecords().stream().map(RechargeResConverter::recharge).toList(),
                value.getTotal());
    }

    @GetMapping("/{rechargeNo}")
    @Operation(summary = "充值单详情")
    @RequiresPermissions("trade:wallet-recharge:query")
    public R<WalletRechargeRes> detail(@PathVariable String rechargeNo) {
        var value = detail.execute(rechargeNo);
        return value == null ? R.fail("充值单不存在") : R.ok(RechargeResConverter.recharge(value));
    }

    @PostMapping("/{rechargeNo}/confirm")
    @Operation(summary = "确认充值到账")
    @RequiresPermissions("trade:wallet-recharge:confirm")
    public R<WalletRechargeRes> confirm(@PathVariable String rechargeNo,
            @Valid @RequestBody WalletRechargeConfirmReq req) {
        return R.ok(RechargeResConverter.recharge(confirm.execute(rechargeNo, req.channelCode())));
    }

    @PostMapping("/{rechargeNo}/refund")
    @Operation(summary = "充值退款")
    @RequiresPermissions("trade:wallet-recharge:refund")
    public R<WalletRechargeRes> refund(@PathVariable String rechargeNo) {
        return R.ok(RechargeResConverter.recharge(refund.execute(rechargeNo)));
    }
}
