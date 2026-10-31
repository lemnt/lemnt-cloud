package com.lemnt.trade.adapter.web.recharge;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.recharge.converter.RechargeResConverter;
import com.lemnt.trade.adapter.web.recharge.req.RechargePackageCreateReq;
import com.lemnt.trade.adapter.web.recharge.req.RechargePackagePageReq;
import com.lemnt.trade.adapter.web.recharge.req.RechargePackageUpdateReq;
import com.lemnt.trade.adapter.web.recharge.res.RechargePackageRes;
import com.lemnt.trade.application.payment.executor.RechargePackageCreateCmdExe;
import com.lemnt.trade.application.payment.executor.RechargePackageDeleteCmdExe;
import com.lemnt.trade.application.payment.executor.RechargePackageDetailQueryExe;
import com.lemnt.trade.application.payment.executor.RechargePackagePageQueryExe;
import com.lemnt.trade.application.payment.executor.RechargePackageUpdateCmdExe;

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

/** 后台充值套餐管理。 */
@Tag(name = "充值套餐")
@RestController
@RequestMapping("/trade/admin/recharge-packages")
@RequiredArgsConstructor
public class RechargePackageController {
    private final RechargePackagePageQueryExe page;
    private final RechargePackageDetailQueryExe detail;
    private final RechargePackageCreateCmdExe create;
    private final RechargePackageUpdateCmdExe update;
    private final RechargePackageDeleteCmdExe delete;

    @GetMapping
    @Operation(summary = "充值套餐分页")
    @RequiresPermissions("trade:recharge-package:list")
    public R<RechargePackageRes> page(@Valid @ModelAttribute RechargePackagePageReq req) {
        var value = page.execute(RechargeResConverter.packagePage(req));
        return R.page(value.getRecords().stream().map(RechargeResConverter::packageView).toList(),
                value.getTotal());
    }

    @GetMapping("/{id}")
    @Operation(summary = "充值套餐详情")
    @RequiresPermissions("trade:recharge-package:query")
    public R<RechargePackageRes> detail(@PathVariable Long id) {
        var value = detail.execute(id);
        return value == null ? R.fail("套餐不存在") : R.ok(RechargeResConverter.packageView(value));
    }

    @PostMapping
    @Operation(summary = "新增充值套餐")
    @RequiresPermissions("trade:recharge-package:create")
    public R<Long> create(@Valid @RequestBody RechargePackageCreateReq req) {
        return R.ok(create.execute(RechargeResConverter.packageCreate(req)).id());
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改充值套餐")
    @RequiresPermissions("trade:recharge-package:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody RechargePackageUpdateReq req) {
        return R.ok(update.execute(RechargeResConverter.packageUpdate(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除充值套餐")
    @RequiresPermissions("trade:recharge-package:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }
}
