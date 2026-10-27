package com.lemnt.trade.adapter.web.outbox;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.application.outbox.executor.TradeOutboxReplayCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 交易可靠事件补偿。
 */
@Tag(name = "交易可靠事件补偿")
@RestController
@RequestMapping("/trade/outbox")
@RequiredArgsConstructor
public class TradeOutboxController {
    private final TradeOutboxReplayCmdExe tradeOutboxReplayCmdExe;

    @Operation(summary = "重放已耗尽重试的 DEAD 事件")
    @RequiresPermissions("trade:outbox:replay")
    @PostMapping("/{eventId}/replay")
    public R<Boolean> replay(@PathVariable String eventId) {
        return R.ok(tradeOutboxReplayCmdExe.execute(eventId));
    }
}
