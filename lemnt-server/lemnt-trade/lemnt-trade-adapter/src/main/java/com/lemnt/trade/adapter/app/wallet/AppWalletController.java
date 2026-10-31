package com.lemnt.trade.adapter.app.wallet;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.trade.adapter.app.wallet.req.AppWalletTransactionPageReq;
import com.lemnt.trade.adapter.app.wallet.req.AppWalletTransferCreateReq;
import com.lemnt.trade.adapter.app.wallet.req.AppWalletTransferPageReq;
import com.lemnt.trade.adapter.web.wallet.converter.WalletResConverter;
import com.lemnt.trade.adapter.web.wallet.converter.WalletTransferResConverter;
import com.lemnt.trade.adapter.web.wallet.res.WalletRes;
import com.lemnt.trade.adapter.web.wallet.res.WalletTransactionRes;
import com.lemnt.trade.adapter.web.wallet.res.WalletTransferRes;
import com.lemnt.trade.application.payment.cmd.WalletTransferCreateCmd;
import com.lemnt.trade.application.payment.executor.MemberWalletQueryExe;
import com.lemnt.trade.application.payment.executor.MemberWalletTransactionPageQueryExe;
import com.lemnt.trade.application.payment.executor.MemberWalletTransferPageQueryExe;
import com.lemnt.trade.application.payment.executor.WalletTransferCreateCmdExe;
import com.lemnt.trade.application.payment.qry.WalletTransactionPageQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** App 本人钱包查询与转账，身份只取认证上下文。 */
@Tag(name = "本人钱包")
@RestController
@RequestMapping("/trade/app/wallet")
@RequiredArgsConstructor
public class AppWalletController {
    private final MemberWalletQueryExe wallet;
    private final MemberWalletTransactionPageQueryExe transactions;
    private final WalletTransferCreateCmdExe createTransfer;
    private final MemberWalletTransferPageQueryExe transfers;

    /** 本人钱包余额，首次访问创建零余额账户。 */
    @GetMapping
    @Operation(summary = "本人钱包")
    public R<WalletRes> wallet() {
        return R.ok(WalletResConverter.wallet(wallet.execute(SecurityUtils.getUserId())));
    }

    /** 本人钱包流水。 */
    @GetMapping("/transactions")
    @Operation(summary = "本人钱包流水")
    public R<WalletTransactionRes> transactions(@Valid @ModelAttribute AppWalletTransactionPageReq req) {
        var value = transactions.execute(SecurityUtils.getUserId(), new WalletTransactionPageQry(req.bizType(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 10 : req.pageSize()));
        return R.page(value.getRecords().stream().map(WalletResConverter::transaction).toList(), value.getTotal());
    }

    /** 本人钱包转账，requestNo 幂等。 */
    @PostMapping("/transfers")
    @Operation(summary = "钱包转账")
    public R<WalletTransferRes> transfer(@Valid @RequestBody AppWalletTransferCreateReq req) {
        var cmd = new WalletTransferCreateCmd(req.toMemberId(), req.amount(), req.remark(), req.requestNo());
        return R.ok(WalletTransferResConverter.transfer(createTransfer.execute(SecurityUtils.getUserId(), cmd)));
    }

    /** 本人转出或转入分页。 */
    @GetMapping("/transfers")
    @Operation(summary = "本人转账分页")
    public R<WalletTransferRes> transfers(@Valid @ModelAttribute AppWalletTransferPageReq req) {
        var value = transfers.execute(SecurityUtils.getUserId(), req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 10 : req.pageSize());
        return R.page(value.getRecords().stream().map(WalletTransferResConverter::transfer).toList(),
                value.getTotal());
    }
}
