package com.lemnt.trade.adapter.web.wallet;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.wallet.converter.WalletResConverter;
import com.lemnt.trade.adapter.web.wallet.req.WalletAdjustReq;
import com.lemnt.trade.adapter.web.wallet.req.WalletPageReq;
import com.lemnt.trade.adapter.web.wallet.req.WalletTransactionPageReq;
import com.lemnt.trade.adapter.web.wallet.res.WalletRes;
import com.lemnt.trade.adapter.web.wallet.res.WalletTransactionRes;
import com.lemnt.trade.application.payment.executor.WalletAdjustCmdExe;
import com.lemnt.trade.application.payment.executor.WalletDetailQueryExe;
import com.lemnt.trade.application.payment.executor.WalletPageQueryExe;
import com.lemnt.trade.application.payment.executor.WalletTransactionPageQueryExe;

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

/** 后台会员钱包管理接口。 */
@Tag(name = "会员钱包管理")
@RestController
@RequestMapping("/trade/admin/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletPageQueryExe page;
    private final WalletDetailQueryExe detail;
    private final WalletTransactionPageQueryExe transactions;
    private final WalletAdjustCmdExe adjust;

    /** 钱包分页。 */
    @GetMapping
    @Operation(summary = "钱包分页")
    @RequiresPermissions("trade:wallet:list")
    public R<WalletRes> page(@Valid @ModelAttribute WalletPageReq req) {
        var value = page.execute(WalletResConverter.page(req));
        return R.page(value.getRecords().stream().map(WalletResConverter::wallet).toList(), value.getTotal());
    }

    /** 钱包详情。 */
    @GetMapping("/{memberId}")
    @Operation(summary = "钱包详情")
    @RequiresPermissions("trade:wallet:query")
    public R<WalletRes> detail(@PathVariable Long memberId) {
        var value = detail.execute(memberId);
        return value == null ? R.fail("钱包不存在") : R.ok(WalletResConverter.wallet(value));
    }

    /** 钱包流水分页。 */
    @GetMapping("/{memberId}/transactions")
    @Operation(summary = "钱包流水分页")
    @RequiresPermissions("trade:wallet:query")
    public R<WalletTransactionRes> transactions(@PathVariable Long memberId,
            @Valid @ModelAttribute WalletTransactionPageReq req) {
        var value = transactions.execute(memberId, WalletResConverter.transactions(req));
        return R.page(value.getRecords().stream().map(WalletResConverter::transaction).toList(), value.getTotal());
    }

    /** 人工调整余额，requestNo 保证重复提交不重复记账。 */
    @PostMapping("/{memberId}/adjust")
    @Operation(summary = "调整钱包余额")
    @RequiresPermissions("trade:wallet:adjust")
    public R<WalletRes> adjust(@PathVariable Long memberId, @Valid @RequestBody WalletAdjustReq req) {
        return R.ok(WalletResConverter.wallet(adjust.execute(memberId, WalletResConverter.adjust(req))));
    }
}
