package com.lemnt.trade.adapter.web;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.application.executor.TradeOutboxReplayCmdExe;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "交易可靠事件补偿")
@RestController
@RequestMapping("/trade/outbox")
@RequiredArgsConstructor
public class TradeOutboxController {
    private final TradeOutboxReplayCmdExe replay;

    @Operation(summary = "重放已耗尽重试的 DEAD 事件")
    @RequiresPermissions("trade:outbox:replay")
    @PostMapping("/{eventId}/replay")
    public R<Boolean> replay(@PathVariable String eventId) {
        return R.ok(replay.execute(eventId));
    }
}
