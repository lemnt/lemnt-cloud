package com.lemnt.trade.adapter.web.store;

import com.lemnt.common.core.domain.R;
import com.lemnt.trade.application.checkout.service.MemberSessionService;
import com.lemnt.trade.application.store.executor.*;
import com.lemnt.trade.application.store.cmd.CouponClaimCmd;
import com.lemnt.trade.adapter.web.store.req.*;
import com.lemnt.trade.adapter.web.store.res.*;
import com.lemnt.trade.adapter.web.store.converter.*;
import com.lemnt.trade.adapter.web.checkout.res.CouponRes;
import com.lemnt.trade.adapter.web.checkout.converter.CheckoutResConverter;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.tags.Tag(name = "小程序领券与加购")
@RestController
@RequestMapping("/trade/app")
@lombok.RequiredArgsConstructor
public class AppPromotionController {
    private final MemberSessionService sessions;
    private final StorefrontCmdExe commands;
    private final PromotionQueryExe queries;

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.UNAUTHORIZED)
    public R<Void> denied() {
        return R.fail("会员登录已失效");
    }

    @GetMapping("/coupon-center")
    @io.swagger.v3.oas.annotations.Operation(summary = "本人可见的公开领券中心")
    public R<PromotionPageRes> offers(
            @RequestHeader(value = "Authorization", required = false) String token,
            @jakarta.validation.Valid @ModelAttribute PromotionPageReq req) {
        return R.ok(
                PromotionResConverter.page(
                        queries.execute(
                                PromotionReqConverter.offers(sessions.requireMember(token), req))));
    }

    @GetMapping("/coupon-wallet")
    @io.swagger.v3.oas.annotations.Operation(summary = "本人优惠券及服务端当前可用状态")
    public R<PromotionPageRes> wallet(
            @RequestHeader(value = "Authorization", required = false) String token,
            @jakarta.validation.Valid @ModelAttribute PromotionPageReq req) {
        return R.ok(
                PromotionResConverter.page(
                        queries.execute(
                                PromotionReqConverter.wallet(sessions.requireMember(token), req))));
    }

    @PostMapping("/coupon-center/{templateId}/claim")
    @io.swagger.v3.oas.annotations.Operation(summary = "本人领取公开优惠券，每模板限一张，重复返回原券")
    public R<CouponRes> claim(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long templateId) {
        return R.ok(
                CheckoutResConverter.coupon(
                        commands.execute(
                                new CouponClaimCmd(sessions.requireMember(token), templateId))));
    }

    @PostMapping("/cart/{productId}/add")
    @io.swagger.v3.oas.annotations.Operation(summary = "会员范围内幂等增量加购，返回首次结果快照")
    public R<CartAdditionRes> add(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long productId,
            @jakarta.validation.Valid @RequestBody CartAddReq req) {
        return R.ok(
                PromotionResConverter.add(
                        commands.execute(
                                PromotionReqConverter.add(
                                        sessions.requireMember(token), productId, req))));
    }
}
