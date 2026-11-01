package com.lemnt.trade.adapter.app.brokerage;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.trade.adapter.app.brokerage.converter.AppBrokerageConverter;
import com.lemnt.trade.adapter.app.brokerage.req.AppBrokerageBindReq;
import com.lemnt.trade.adapter.app.brokerage.req.AppBrokeragePageReq;
import com.lemnt.trade.adapter.app.brokerage.req.AppBrokerageWithdrawCreateReq;
import com.lemnt.trade.adapter.app.brokerage.res.AppBrokerageRecordRes;
import com.lemnt.trade.adapter.app.brokerage.res.AppBrokerageUserRes;
import com.lemnt.trade.adapter.app.brokerage.res.AppBrokerageWithdrawRes;
import com.lemnt.trade.application.brokerage.executor.AppBrokerageUserQueryExe;
import com.lemnt.trade.application.brokerage.executor.BrokerageBindCmdExe;
import com.lemnt.trade.application.brokerage.executor.BrokerageRecordPageQueryExe;
import com.lemnt.trade.application.brokerage.executor.BrokerageWithdrawCreateCmdExe;
import com.lemnt.trade.application.brokerage.executor.BrokerageWithdrawPageQueryExe;

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

/** 会员分销中心。 */
@Tag(name = "会员分销")
@RestController
@RequestMapping("/trade/app/brokerage")
@RequiredArgsConstructor
public class AppBrokerageController {
    private final AppBrokerageUserQueryExe user;
    private final BrokerageBindCmdExe bind;
    private final BrokerageRecordPageQueryExe records;
    private final BrokerageWithdrawCreateCmdExe withdrawCreate;
    private final BrokerageWithdrawPageQueryExe withdraws;

    @GetMapping("/user")
    @Operation(summary = "本人分销信息")
    public R<AppBrokerageUserRes> user() {
        return R.ok(AppBrokerageConverter.user(user.execute(SecurityUtils.getUserId())));
    }

    @PostMapping("/bind")
    @Operation(summary = "绑定推广员")
    public R<Boolean> bind(@Valid @RequestBody AppBrokerageBindReq req) {
        return R.ok(bind.execute(AppBrokerageConverter.bind(SecurityUtils.getUserId(), req)));
    }

    @GetMapping("/records")
    @Operation(summary = "本人佣金记录")
    public R<AppBrokerageRecordRes> records(@Valid @ModelAttribute AppBrokeragePageReq req) {
        var result = records.execute(AppBrokerageConverter.records(SecurityUtils.getUserId(), req));
        return R.page(result.getRecords().stream().map(AppBrokerageConverter::record).toList(),
                result.getTotal());
    }

    @GetMapping("/withdraws")
    @Operation(summary = "本人提现记录")
    public R<AppBrokerageWithdrawRes> withdraws(@Valid @ModelAttribute AppBrokeragePageReq req) {
        var result = withdraws.execute(AppBrokerageConverter.withdraws(SecurityUtils.getUserId(), req));
        return R.page(result.getRecords().stream().map(AppBrokerageConverter::withdraw).toList(),
                result.getTotal());
    }

    @PostMapping("/withdraws")
    @Operation(summary = "申请佣金提现")
    public R<Long> withdraw(@Valid @RequestBody AppBrokerageWithdrawCreateReq req) {
        return R.ok(withdrawCreate.execute(
                AppBrokerageConverter.withdraw(SecurityUtils.getUserId(), req)));
    }
}
