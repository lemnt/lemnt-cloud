package com.lemnt.trade.adapter.web.paychannel;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.payapp.converter.PayConfigReqConverter;
import com.lemnt.trade.adapter.web.payapp.converter.PayConfigResConverter;
import com.lemnt.trade.adapter.web.paychannel.req.PayChannelCreateReq;
import com.lemnt.trade.adapter.web.paychannel.req.PayChannelPageReq;
import com.lemnt.trade.adapter.web.paychannel.req.PayChannelUpdateReq;
import com.lemnt.trade.adapter.web.paychannel.res.PayChannelConfigRes;
import com.lemnt.trade.application.payment.executor.PayChannelCreateCmdExe;
import com.lemnt.trade.application.payment.executor.PayChannelDeleteCmdExe;
import com.lemnt.trade.application.payment.executor.PayChannelDetailQueryExe;
import com.lemnt.trade.application.payment.executor.PayChannelListByAppQueryExe;
import com.lemnt.trade.application.payment.executor.PayChannelPageQueryExe;
import com.lemnt.trade.application.payment.executor.PayChannelUpdateCmdExe;

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

import java.util.List;

/** 支付渠道配置管理接口，不返回渠道配置明文。 */
@Tag(name = "支付渠道配置")
@RestController
@RequestMapping("/trade/admin/pay-channels")
@RequiredArgsConstructor
public class PayChannelConfigController {
    private final PayChannelCreateCmdExe create;
    private final PayChannelUpdateCmdExe update;
    private final PayChannelDeleteCmdExe delete;
    private final PayChannelDetailQueryExe detail;
    private final PayChannelPageQueryExe page;
    private final PayChannelListByAppQueryExe listByApp;

    /** 新增支付渠道配置。 */
    @PostMapping
    @Operation(summary = "新增支付渠道配置")
    @RequiresPermissions("trade:pay-channel:create")
    public R<Long> create(@Valid @RequestBody PayChannelCreateReq req) {
        return R.ok(create.execute(PayConfigReqConverter.payChannelCreate(req)).id());
    }

    /** 修改支付渠道配置，配置留空保留原值。 */
    @PutMapping("/{id}")
    @Operation(summary = "修改支付渠道配置")
    @RequiresPermissions("trade:pay-channel:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody PayChannelUpdateReq req) {
        return R.ok(update.execute(PayConfigReqConverter.payChannelUpdate(id, req)));
    }

    /** 删除支付渠道配置。 */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除支付渠道配置")
    @RequiresPermissions("trade:pay-channel:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    /** 渠道配置详情，不含配置明文。 */
    @GetMapping("/{id}")
    @Operation(summary = "支付渠道配置详情")
    @RequiresPermissions("trade:pay-channel:query")
    public R<PayChannelConfigRes> detail(@PathVariable Long id) {
        var value = detail.execute(id);
        return value == null ? R.fail("渠道配置不存在") : R.ok(PayConfigResConverter.payChannel(value));
    }

    /** 渠道配置分页。 */
    @GetMapping
    @Operation(summary = "支付渠道配置分页")
    @RequiresPermissions("trade:pay-channel:list")
    public R<PayChannelConfigRes> page(@Valid @ModelAttribute PayChannelPageReq req) {
        var value = page.execute(PayConfigReqConverter.payChannelPage(req));
        return R.page(value.getRecords().stream().map(PayConfigResConverter::payChannel).toList(), value.getTotal());
    }

    /** 查询应用下全部渠道配置，供选择使用。 */
    @GetMapping("/by-app/{appId}")
    @Operation(summary = "应用下渠道配置")
    @RequiresPermissions("trade:pay-channel:query")
    public R<List<PayChannelConfigRes>> listByApp(@PathVariable Long appId) {
        return R.ok(listByApp.execute(appId).stream().map(PayConfigResConverter::payChannel).toList());
    }
}
