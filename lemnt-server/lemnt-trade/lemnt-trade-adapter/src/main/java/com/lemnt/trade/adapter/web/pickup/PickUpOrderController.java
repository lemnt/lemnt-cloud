package com.lemnt.trade.adapter.web.pickup;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.trade.adapter.web.pickup.converter.PickUpConverter;
import com.lemnt.trade.adapter.web.pickup.req.PickUpAssignReq;
import com.lemnt.trade.adapter.web.pickup.res.PickUpOrderRes;
import com.lemnt.trade.application.pickup.cmd.PickUpAssignCmd;
import com.lemnt.trade.application.pickup.cmd.PickUpVerifyCmd;
import com.lemnt.trade.application.pickup.executor.PickUpAssignCmdExe;
import com.lemnt.trade.application.pickup.executor.PickUpPreviewQueryExe;
import com.lemnt.trade.application.pickup.executor.PickUpVerifyCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 后台自提订单与核销。 */
@Tag(name = "自提核销")
@RestController
@RequestMapping("/trade/admin/orders/pick-up")
@RequiredArgsConstructor
public class PickUpOrderController {
    private final PickUpAssignCmdExe assign;
    private final PickUpPreviewQueryExe preview;
    private final PickUpVerifyCmdExe verify;

    @PostMapping
    @Operation(summary = "将已支付订单设置为自提")
    @RequiresPermissions("trade:fulfillment:update")
    public R<PickUpOrderRes> assign(@Valid @RequestBody PickUpAssignReq req) {
        return R.ok(PickUpConverter.order(assign.execute(new PickUpAssignCmd(req.orderNo(), req.storeId(),
                SecurityUtils.getUserId()))));
    }

    @GetMapping("/{code}")
    @Operation(summary = "按核销码预览自提订单")
    @RequiresPermissions("trade:fulfillment:query")
    public R<PickUpOrderRes> preview(@PathVariable String code) {
        return R.ok(PickUpConverter.order(preview.execute(code)));
    }

    @PostMapping("/{code}/verify")
    @Operation(summary = "确认自提核销")
    @RequiresPermissions("trade:fulfillment:update")
    public R<PickUpOrderRes> verify(@PathVariable String code) {
        return R.ok(PickUpConverter.order(verify.execute(new PickUpVerifyCmd(code, SecurityUtils.getUserId()))));
    }
}
