package com.lemnt.mall.adapter.web.coupon;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.coupon.converter.CouponReqConverter;
import com.lemnt.mall.adapter.web.coupon.converter.CouponResConverter;
import com.lemnt.mall.adapter.web.coupon.req.CouponTemplateCreateReq;
import com.lemnt.mall.adapter.web.coupon.req.CouponTemplateEnableReq;
import com.lemnt.mall.adapter.web.coupon.req.CouponTemplateUpdateReq;
import com.lemnt.mall.adapter.web.coupon.res.MallCouponTemplateCreateRes;
import com.lemnt.mall.adapter.web.coupon.res.MallCouponTemplateTemplatesRes;
import com.lemnt.mall.application.coupon.executor.CouponTemplateCreateCmdExe;
import com.lemnt.mall.application.coupon.executor.CouponTemplateDeleteCmdExe;
import com.lemnt.mall.application.coupon.executor.CouponTemplateEnableCmdExe;
import com.lemnt.mall.application.coupon.executor.CouponTemplateListQueryExe;
import com.lemnt.mall.application.coupon.executor.CouponTemplateUpdateCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final CouponTemplateUpdateCmdExe couponTemplateUpdateCmdExe;
    private final CouponTemplateDeleteCmdExe couponTemplateDeleteCmdExe;

    @Operation(summary = "查询模板，超过1000条时拒绝截断")
    @GetMapping("/coupon-templates")
    @RequiresPermissions("mall:coupon-template:list")
    public R<List<MallCouponTemplateTemplatesRes>> templates() {
        return R.ok(
                couponTemplateListQueryExe.execute().stream().map(CouponResConverter::couponTemplateToMallCouponTemplateTemplatesRes).toList());
    }

    @Operation(summary = "创建优惠券模板")
    @PostMapping("/coupon-templates")
    @RequiresPermissions("mall:coupon-template:add")
    public R<MallCouponTemplateCreateRes> create(@Valid @RequestBody CouponTemplateCreateReq req) {
        return R.ok(
                CouponResConverter.couponTemplateToMallCouponTemplateCreateRes(
                couponTemplateCreateCmdExe.execute(CouponReqConverter.template(req))));
    }

    @Operation(summary = "启停后续发券，不影响已发券权益")
    @PutMapping("/coupon-templates/{id}/enabled")
    @RequiresPermissions("mall:coupon-template:edit")
    public R<Boolean> enable(
            @PathVariable Long id, @Valid @RequestBody CouponTemplateEnableReq req) {
        return R.ok(couponTemplateEnableCmdExe.execute(CouponReqConverter.enable(id, req)));
    }
    /** 更新模板只影响之后领取的优惠券。 */
    @PutMapping("/coupon-templates/{id}")
    @Operation(summary = "修改优惠券模板")
    @RequiresPermissions("mall:coupon-template:edit")
    public R<Boolean> update(@PathVariable Long id,
            @Valid @RequestBody CouponTemplateUpdateReq req) {
        return R.ok(couponTemplateUpdateCmdExe.execute(CouponReqConverter.update(id, req)));
    }

    /** 删除模板不撤回会员已领取的券。 */
    @DeleteMapping("/coupon-templates/{id}")
    @Operation(summary = "删除优惠券模板")
    @RequiresPermissions("mall:coupon-template:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(couponTemplateDeleteCmdExe.execute(id));
    }
}
