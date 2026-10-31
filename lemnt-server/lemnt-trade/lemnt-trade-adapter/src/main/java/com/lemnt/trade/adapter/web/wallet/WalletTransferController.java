package com.lemnt.trade.adapter.web.wallet;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.wallet.converter.WalletTransferResConverter;
import com.lemnt.trade.adapter.web.wallet.req.WalletTransferPageReq;
import com.lemnt.trade.adapter.web.wallet.res.WalletTransferRes;
import com.lemnt.trade.application.payment.executor.WalletTransferDetailQueryExe;
import com.lemnt.trade.application.payment.executor.WalletTransferPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 后台钱包转账查询。 */
@Tag(name = "钱包转账")
@RestController
@RequestMapping("/trade/admin/wallet-transfers")
@RequiredArgsConstructor
public class WalletTransferController {
    private final WalletTransferPageQueryExe page;
    private final WalletTransferDetailQueryExe detail;

    @GetMapping
    @Operation(summary = "钱包转账分页")
    @RequiresPermissions("trade:wallet-transfer:list")
    public R<WalletTransferRes> page(@Valid @ModelAttribute WalletTransferPageReq req) {
        var value = page.execute(WalletTransferResConverter.page(req));
        return R.page(value.getRecords().stream().map(WalletTransferResConverter::transfer).toList(),
                value.getTotal());
    }

    @GetMapping("/{transferNo}")
    @Operation(summary = "钱包转账详情")
    @RequiresPermissions("trade:wallet-transfer:query")
    public R<WalletTransferRes> detail(@PathVariable String transferNo) {
        var value = detail.execute(transferNo);
        return value == null ? R.fail("转账记录不存在") : R.ok(WalletTransferResConverter.transfer(value));
    }
}
