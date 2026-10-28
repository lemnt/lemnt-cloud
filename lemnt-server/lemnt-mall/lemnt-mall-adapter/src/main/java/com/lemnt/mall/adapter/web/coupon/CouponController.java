package com.lemnt.mall.adapter.web.coupon;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.coupon.converter.CouponReqConverter;
import com.lemnt.mall.adapter.web.coupon.converter.CouponResConverter;
import com.lemnt.mall.adapter.web.coupon.req.CouponManagementPageReq;
import com.lemnt.mall.adapter.web.coupon.res.CouponManagementPageRes;
import com.lemnt.mall.adapter.web.coupon.res.MallCouponCouponsRes;
import com.lemnt.mall.adapter.web.coupon.res.MallCouponIssueRes;
import com.lemnt.mall.application.coupon.cmd.CouponIssueCmd;
import com.lemnt.mall.application.coupon.executor.CouponIssueCmdExe;
import com.lemnt.mall.application.coupon.executor.CouponListQueryExe;
import com.lemnt.mall.application.coupon.executor.CouponManagementPageQueryExe;
import com.lemnt.mall.application.coupon.executor.CouponRevokeCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 会员优惠券接口。 */
@Tag(name = "会员优惠券")
@RestController
@RequestMapping("/mall/admin")
@RequiredArgsConstructor
public class CouponController {
    private final CouponIssueCmdExe couponIssueCmdExe;
    private final CouponRevokeCmdExe couponRevokeCmdExe;
    private final CouponListQueryExe couponListQueryExe;
    private final CouponManagementPageQueryExe couponManagementPageQueryExe;

    @Operation(summary = "为目标会员发券，每人每模板限一张，同次重试幂等")
    @PostMapping("/members/{memberId}/coupons/{templateId}")
    @RequiresPermissions("mall:coupon:issue")
    public R<MallCouponIssueRes> issue(@PathVariable Long memberId, @PathVariable Long templateId) {
        return R.ok(
                CouponResConverter.couponToMallCouponIssueRes(
                couponIssueCmdExe.execute(new CouponIssueCmd(memberId, templateId))));
    }

    @Operation(summary = "查看目标会员优惠券，超过1000条时拒绝截断")
    @GetMapping("/members/{memberId}/coupons")
    @RequiresPermissions("mall:coupon:list")
    public R<List<MallCouponCouponsRes>> coupons(@PathVariable Long memberId) {
        return R.ok(couponListQueryExe.execute(memberId).stream().map(CouponResConverter::couponToMallCouponCouponsRes).toList());
    }
    /** 撤回会员未用或已过期券。 */
    @DeleteMapping("/members/{memberId}/coupons/{couponId}")
    @Operation(summary = "撤回会员优惠券")
    @RequiresPermissions("mall:coupon:remove")
    public R<Boolean> revoke(@PathVariable Long memberId, @PathVariable Long couponId) {
        return R.ok(couponRevokeCmdExe.execute(memberId, couponId));
    }
    /** 后台持券管理可按会员、模板和状态分页查询。 */
    @GetMapping("/coupons")
    @Operation(summary = "会员优惠券分页")
    @RequiresPermissions("mall:coupon:list")
    public R<CouponManagementPageRes> page(
            @jakarta.validation.Valid @ModelAttribute CouponManagementPageReq req) {
        var page = couponManagementPageQueryExe.execute(CouponReqConverter.page(req));
        return R.page(page.records().stream().map(CouponResConverter::page).toList(), page.total());
    }
}
