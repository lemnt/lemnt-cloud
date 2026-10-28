package com.lemnt.trade.adapter.app.aftersale;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.trade.application.aftersale.executor.AfterSaleCancelCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/** 会员本人售后操作。 */
@Tag(name = "会员售后")
@RestController
@RequiredArgsConstructor
@RequestMapping("/trade/app/after-sales")
public class AppAfterSaleController {
    private final AfterSaleCancelCmdExe cancel;
    /** 不接受memberId参数，状态和归属由服务端判定。 */
    @PostMapping("/{no}/cancel")
    @Operation(summary = "本人取消售后")
    public R<Boolean> cancel(@PathVariable String no) {
        return R.ok(cancel.execute(SecurityUtils.getUserId(), no));
    }
}
