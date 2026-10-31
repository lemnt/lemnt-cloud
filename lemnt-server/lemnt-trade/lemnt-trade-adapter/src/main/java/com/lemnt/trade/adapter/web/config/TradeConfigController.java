package com.lemnt.trade.adapter.web.config;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.config.converter.TradeConfigReqConverter;
import com.lemnt.trade.adapter.web.config.converter.TradeConfigResConverter;
import com.lemnt.trade.adapter.web.config.req.TradeConfigUpdateReq;
import com.lemnt.trade.adapter.web.config.res.TradeConfigDetailRes;
import com.lemnt.trade.application.config.executor.TradeConfigDetailQueryExe;
import com.lemnt.trade.application.config.executor.TradeConfigUpdateCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 交易基础配置接口。 */
@Tag(name = "交易基础配置")
@RestController
@RequestMapping("/trade/admin/config")
@RequiredArgsConstructor
public class TradeConfigController {
    private final TradeConfigDetailQueryExe detail;
    private final TradeConfigUpdateCmdExe update;

    /** 读取完整交易配置。 */
    @GetMapping
    @Operation(summary = "查询交易配置")
    @RequiresPermissions("trade:config:query")
    public R<TradeConfigDetailRes> detail() {
        return R.ok(TradeConfigResConverter.tradeConfigDetail(detail.execute()));
    }

    /** 保存完整交易配置。 */
    @PutMapping
    @Operation(summary = "修改交易配置")
    @RequiresPermissions("trade:config:update")
    public R<Boolean> update(@Valid @RequestBody TradeConfigUpdateReq req) {
        return R.ok(update.execute(TradeConfigReqConverter.tradeConfigUpdate(req)));
    }
}
