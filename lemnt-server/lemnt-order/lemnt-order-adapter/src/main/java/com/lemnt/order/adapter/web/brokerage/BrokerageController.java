package com.lemnt.order.adapter.web.brokerage;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.order.adapter.web.brokerage.converter.BrokerageConverter;
import com.lemnt.order.adapter.web.brokerage.req.BrokerageBindReq;
import com.lemnt.order.adapter.web.brokerage.req.BrokerageEnabledReq;
import com.lemnt.order.adapter.web.brokerage.req.BrokerageRecordPageReq;
import com.lemnt.order.adapter.web.brokerage.req.BrokerageUserPageReq;
import com.lemnt.order.adapter.web.brokerage.req.BrokerageWithdrawAuditReq;
import com.lemnt.order.adapter.web.brokerage.req.BrokerageWithdrawPageReq;
import com.lemnt.order.adapter.web.brokerage.res.BrokerageRecordRes;
import com.lemnt.order.adapter.web.brokerage.res.BrokerageUserRes;
import com.lemnt.order.adapter.web.brokerage.res.BrokerageWithdrawRes;
import com.lemnt.order.application.brokerage.executor.BrokerageAdminBindCmdExe;
import com.lemnt.order.application.brokerage.executor.BrokerageEnabledCmdExe;
import com.lemnt.order.application.brokerage.executor.BrokerageRecordPageQueryExe;
import com.lemnt.order.application.brokerage.executor.BrokerageUnfreezeCmdExe;
import com.lemnt.order.application.brokerage.executor.BrokerageUserPageQueryExe;
import com.lemnt.order.application.brokerage.executor.BrokerageWithdrawAuditCmdExe;
import com.lemnt.order.application.brokerage.executor.BrokerageWithdrawPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 后台分销用户、佣金与提现管理。 */
@Tag(name = "分销管理")
@RestController
@RequestMapping("/trade/admin/brokerage")
@RequiredArgsConstructor
public class BrokerageController {
    private final BrokerageUserPageQueryExe users;
    private final BrokerageEnabledCmdExe enabled;
    private final BrokerageAdminBindCmdExe bind;
    private final BrokerageRecordPageQueryExe records;
    private final BrokerageWithdrawPageQueryExe withdraws;
    private final BrokerageWithdrawAuditCmdExe audit;
    private final BrokerageUnfreezeCmdExe unfreeze;

    @GetMapping("/users")
    @Operation(summary = "分销用户分页")
    @RequiresPermissions("trade:brokerage:list")
    public R<BrokerageUserRes> users(@Valid @ModelAttribute BrokerageUserPageReq req) {
        var result = users.execute(BrokerageConverter.users(req));
        return R.page(
                result.getRecords().stream().map(BrokerageConverter::user).toList(),
                result.getTotal());
    }

    @PutMapping("/users/{id}/enabled")
    @Operation(summary = "开通或取消分销资格")
    @RequiresPermissions("trade:brokerage:update")
    public R<Boolean> enabled(@PathVariable Long id, @Valid @RequestBody BrokerageEnabledReq req) {
        return R.ok(enabled.execute(BrokerageConverter.enabled(id, req)));
    }

    @PutMapping("/users/{id}/bind")
    @Operation(summary = "修改推广员")
    @RequiresPermissions("trade:brokerage:update")
    public R<Boolean> bind(@PathVariable Long id, @Valid @RequestBody BrokerageBindReq req) {
        return R.ok(bind.execute(BrokerageConverter.bind(id, req)));
    }

    @GetMapping("/records")
    @Operation(summary = "佣金记录分页")
    @RequiresPermissions("trade:brokerage:list")
    public R<BrokerageRecordRes> records(@Valid @ModelAttribute BrokerageRecordPageReq req) {
        var result = records.execute(BrokerageConverter.records(req));
        return R.page(
                result.getRecords().stream().map(BrokerageConverter::record).toList(),
                result.getTotal());
    }

    @GetMapping("/withdraws")
    @Operation(summary = "提现单分页")
    @RequiresPermissions("trade:brokerage:list")
    public R<BrokerageWithdrawRes> withdraws(@Valid @ModelAttribute BrokerageWithdrawPageReq req) {
        var result = withdraws.execute(BrokerageConverter.withdraws(req));
        return R.page(
                result.getRecords().stream().map(BrokerageConverter::withdraw).toList(),
                result.getTotal());
    }

    @PostMapping("/withdraws/{id}/audit")
    @Operation(summary = "审核提现")
    @RequiresPermissions("trade:brokerage:audit")
    public R<Boolean> audit(
            @PathVariable Long id, @Valid @RequestBody BrokerageWithdrawAuditReq req) {
        return R.ok(audit.execute(BrokerageConverter.audit(id, SecurityUtils.getUserId(), req)));
    }

    @PostMapping("/unfreeze")
    @Operation(summary = "手动触发到期佣金解冻")
    @RequiresPermissions("trade:brokerage:update")
    public R<Integer> unfreeze(@RequestParam(defaultValue = "50") Integer limit) {
        return R.ok(unfreeze.execute(limit));
    }
}
