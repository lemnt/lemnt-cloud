package com.lemnt.trade.adapter.web.checkout;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.checkout.req.*;
import com.lemnt.trade.adapter.web.checkout.res.*;
import com.lemnt.trade.adapter.web.checkout.converter.*;
import com.lemnt.trade.application.checkout.executor.*;
import com.lemnt.trade.application.checkout.cmd.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Tag(name = "后台优惠券管理")
@RestController
@RequestMapping("/trade/admin")
@RequiredArgsConstructor
public class AdminCouponController {
    private final CouponTemplateListQueryExe templates;
    private final CouponTemplateCreateCmdExe create;
    private final CouponTemplateEnableCmdExe enable;
    private final CouponIssueCmdExe issue;
    private final CouponListQueryExe coupons;

    @Operation(summary = "查询模板，超过1000条时拒绝截断")
    @GetMapping("/coupon-templates")
    @RequiresPermissions("trade:coupon-template:list")
    public R<List<CouponTemplateRes>> templates() {
        return R.ok(
                templates.execute().stream().map(CheckoutResConverter::couponTemplate).toList());
    }

    @Operation(summary = "创建规则不可变的优惠券模板")
    @PostMapping("/coupon-templates")
    @RequiresPermissions("trade:coupon-template:add")
    public R<CouponTemplateRes> create(@Valid @RequestBody CouponTemplateCreateReq req) {
        return R.ok(
                CheckoutResConverter.couponTemplate(
                        create.execute(CheckoutReqConverter.template(req))));
    }

    @Operation(summary = "启停新发放与新兑换，不影响已锁定订单")
    @PutMapping("/coupon-templates/{id}/enabled")
    @RequiresPermissions("trade:coupon-template:edit")
    public R<Boolean> enable(
            @PathVariable Long id, @Valid @RequestBody CouponTemplateEnableReq req) {
        return R.ok(enable.execute(CheckoutReqConverter.enable(id, req)));
    }

    @Operation(summary = "为目标会员发券，每人每模板限一张，同次重试幂等")
    @PostMapping("/members/{memberId}/coupons/{templateId}")
    @RequiresPermissions("trade:coupon:issue")
    public R<CouponRes> issue(@PathVariable Long memberId, @PathVariable Long templateId) {
        return R.ok(
                CheckoutResConverter.coupon(
                        issue.execute(new CouponIssueCmd(memberId, templateId))));
    }

    @Operation(summary = "查看目标会员优惠券，超过1000条时拒绝截断")
    @GetMapping("/members/{memberId}/coupons")
    @RequiresPermissions("trade:coupon:list")
    public R<List<CouponRes>> coupons(@PathVariable Long memberId) {
        return R.ok(coupons.execute(memberId).stream().map(CheckoutResConverter::coupon).toList());
    }
}
