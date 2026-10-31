package com.lemnt.trade.adapter.web.payapp;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.payapp.converter.PayConfigReqConverter;
import com.lemnt.trade.adapter.web.payapp.converter.PayConfigResConverter;
import com.lemnt.trade.adapter.web.payapp.req.PayAppCreateReq;
import com.lemnt.trade.adapter.web.payapp.req.PayAppPageReq;
import com.lemnt.trade.adapter.web.payapp.req.PayAppStatusUpdateReq;
import com.lemnt.trade.adapter.web.payapp.req.PayAppUpdateReq;
import com.lemnt.trade.adapter.web.payapp.res.PayAppRes;
import com.lemnt.trade.application.payment.executor.PayAppCreateCmdExe;
import com.lemnt.trade.application.payment.executor.PayAppDeleteCmdExe;
import com.lemnt.trade.application.payment.executor.PayAppDetailQueryExe;
import com.lemnt.trade.application.payment.executor.PayAppPageQueryExe;
import com.lemnt.trade.application.payment.executor.PayAppStatusUpdateCmdExe;
import com.lemnt.trade.application.payment.executor.PayAppUpdateCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 支付应用管理接口。 */
@Tag(name = "支付应用管理")
@RestController
@RequestMapping("/trade/admin/pay-apps")
@RequiredArgsConstructor
public class PayAppController {
    private final PayAppCreateCmdExe create;
    private final PayAppUpdateCmdExe update;
    private final PayAppStatusUpdateCmdExe updateStatus;
    private final PayAppDeleteCmdExe delete;
    private final PayAppDetailQueryExe detail;
    private final PayAppPageQueryExe page;

    /** 新增支付应用。 */
    @PostMapping
    @Operation(summary = "新增支付应用")
    @RequiresPermissions("trade:pay-app:create")
    public R<Long> create(@Valid @RequestBody PayAppCreateReq req) {
        return R.ok(create.execute(PayConfigReqConverter.payAppCreate(req)).id());
    }

    /** 修改支付应用，应用标识不可修改。 */
    @PutMapping("/{id}")
    @Operation(summary = "修改支付应用")
    @RequiresPermissions("trade:pay-app:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody PayAppUpdateReq req) {
        return R.ok(update.execute(PayConfigReqConverter.payAppUpdate(id, req)));
    }

    /** 启用或停用支付应用。 */
    @PutMapping("/{id}/status")
    @Operation(summary = "支付应用状态")
    @RequiresPermissions("trade:pay-app:update")
    public R<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody PayAppStatusUpdateReq req) {
        return R.ok(updateStatus.execute(id, req.status()));
    }

    /** 删除支付应用，仍有渠道配置时拒绝。 */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除支付应用")
    @RequiresPermissions("trade:pay-app:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    /** 支付应用详情。 */
    @GetMapping("/{id}")
    @Operation(summary = "支付应用详情")
    @RequiresPermissions("trade:pay-app:query")
    public R<PayAppRes> detail(@PathVariable Long id) {
        var value = detail.execute(id);
        return value == null ? R.fail("支付应用不存在") : R.ok(PayConfigResConverter.payApp(value));
    }

    /** 支付应用分页。 */
    @GetMapping
    @Operation(summary = "支付应用分页")
    @RequiresPermissions("trade:pay-app:list")
    public R<PayAppRes> page(@Valid @ModelAttribute PayAppPageReq req) {
        var value = page.execute(PayConfigReqConverter.payAppPage(req));
        return R.page(value.getRecords().stream().map(PayConfigResConverter::payApp).toList(), value.getTotal());
    }
}
