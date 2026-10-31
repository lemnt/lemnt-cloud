package com.lemnt.trade.adapter.app.recharge;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.trade.adapter.app.recharge.req.AppRechargeCreateReq;
import com.lemnt.trade.adapter.app.recharge.req.AppRechargePageReq;
import com.lemnt.trade.adapter.web.recharge.converter.RechargeResConverter;
import com.lemnt.trade.adapter.web.recharge.res.RechargePackageRes;
import com.lemnt.trade.adapter.web.recharge.res.WalletRechargeRes;
import com.lemnt.trade.application.payment.cmd.WalletRechargeCreateCmd;
import com.lemnt.trade.application.payment.executor.MemberWalletRechargePageQueryExe;
import com.lemnt.trade.application.payment.executor.RechargePackageEnabledListQueryExe;
import com.lemnt.trade.application.payment.executor.WalletRechargeCreateCmdExe;

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

import java.util.List;

/** App 充值套餐选择与本人充值单。 */
@Tag(name = "本人充值")
@RestController
@RequestMapping("/trade/app")
@RequiredArgsConstructor
public class AppRechargeController {
    private final RechargePackageEnabledListQueryExe packages;
    private final WalletRechargeCreateCmdExe create;
    private final MemberWalletRechargePageQueryExe recharges;

    /** 启用中的充值套餐。 */
    @GetMapping("/recharge-packages")
    @Operation(summary = "充值套餐列表")
    public R<List<RechargePackageRes>> packages() {
        return R.ok(packages.execute().stream().map(RechargeResConverter::packageView).toList());
    }

    /** 按套餐创建充值单，requestNo 幂等。 */
    @PostMapping("/wallet/recharges")
    @Operation(summary = "创建充值单")
    public R<WalletRechargeRes> create(@Valid @RequestBody AppRechargeCreateReq req) {
        var cmd = new WalletRechargeCreateCmd(req.packageId(), req.requestNo());
        return R.ok(RechargeResConverter.recharge(create.execute(SecurityUtils.getUserId(), cmd)));
    }

    /** 本人充值单分页。 */
    @GetMapping("/wallet/recharges")
    @Operation(summary = "本人充值单分页")
    public R<WalletRechargeRes> recharges(@Valid @ModelAttribute AppRechargePageReq req) {
        var value = recharges.execute(SecurityUtils.getUserId(), req.status(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 10 : req.pageSize());
        return R.page(value.getRecords().stream().map(RechargeResConverter::recharge).toList(), value.getTotal());
    }
}
