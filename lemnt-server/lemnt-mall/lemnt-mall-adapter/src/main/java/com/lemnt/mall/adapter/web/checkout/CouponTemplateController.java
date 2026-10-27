package com.lemnt.mall.adapter.web.checkout;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.checkout.converter.CheckoutReqConverter;
import com.lemnt.mall.adapter.web.checkout.converter.CheckoutResConverter;
import com.lemnt.mall.adapter.web.checkout.req.CouponTemplateCreateReq;
import com.lemnt.mall.adapter.web.checkout.req.CouponTemplateEnableReq;
import com.lemnt.mall.adapter.web.checkout.res.MallCouponTemplateCreateRes;
import com.lemnt.mall.adapter.web.checkout.res.MallCouponTemplateTemplatesRes;
import com.lemnt.mall.application.checkout.executor.CouponTemplateCreateCmdExe;
import com.lemnt.mall.application.checkout.executor.CouponTemplateEnableCmdExe;
import com.lemnt.mall.application.checkout.executor.CouponTemplateListQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/** 优惠券模板接口。 */
@Tag(name = "优惠券模板")
@RestController
@RequestMapping("/mall/admin")
@RequiredArgsConstructor
public class CouponTemplateController {
    private final CouponTemplateListQueryExe couponTemplateListQueryExe;
    private final CouponTemplateCreateCmdExe couponTemplateCreateCmdExe;
    private final CouponTemplateEnableCmdExe couponTemplateEnableCmdExe;

    @Operation(summary = "查询模板，超过1000条时拒绝截断")
    @GetMapping("/coupon-templates")
    @RequiresPermissions("mall:coupon-template:list")
    public R<List<MallCouponTemplateTemplatesRes>> templates() {
        return R.ok(
                couponTemplateListQueryExe.execute().stream().map(CheckoutResConverter::couponTemplateToMallCouponTemplateTemplatesRes).toList());
    }

    @Operation(summary = "创建规则不可变的优惠券模板")
    @PostMapping("/coupon-templates")
    @RequiresPermissions("mall:coupon-template:add")
    public R<MallCouponTemplateCreateRes> create(@Valid @RequestBody CouponTemplateCreateReq req) {
        return R.ok(
                CheckoutResConverter.couponTemplateToMallCouponTemplateCreateRes(
                couponTemplateCreateCmdExe.execute(CheckoutReqConverter.template(req))));
    }

    @Operation(summary = "启停新发放与新兑换，不影响已锁定订单")
    @PutMapping("/coupon-templates/{id}/enabled")
    @RequiresPermissions("mall:coupon-template:edit")
    public R<Boolean> enable(
            @PathVariable Long id, @Valid @RequestBody CouponTemplateEnableReq req) {
        return R.ok(couponTemplateEnableCmdExe.execute(CheckoutReqConverter.enable(id, req)));
    }
}
