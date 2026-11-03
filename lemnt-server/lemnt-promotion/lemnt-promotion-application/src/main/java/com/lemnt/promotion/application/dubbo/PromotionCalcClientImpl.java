package com.lemnt.promotion.application.dubbo;
import com.lemnt.promotion.application.dubbo.converter.PromotionQuoteConverter;
import com.lemnt.promotion.application.member.service.MemberValidationService;
import com.lemnt.promotion.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.promotion.application.promotion.cmd.PromotionOrderLineCmd;
import com.lemnt.promotion.application.promotion.service.ActivityOrderPricingService;
import com.lemnt.promotion.application.promotion.service.ActivityPricingService;
import com.lemnt.promotion.client.PromotionCalcClient;
import com.lemnt.promotion.client.bo.*;
import com.lemnt.promotion.client.dto.*;
import com.lemnt.promotion.domain.coupon.gateway.CouponGateway;
import com.lemnt.promotion.domain.coupon.gateway.CouponTemplateGateway;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

import java.util.List;
/** 私有报价入口，不信任调用方传入会员折扣/积分余额，不读取订单库。 */
@DubboService
@RequiredArgsConstructor
public class PromotionCalcClientImpl implements PromotionCalcClient {
    private final ActivityPricingService pricing;
    private final ActivityOrderPricingService activities;
    private final MemberValidationService members;
    private final CouponGateway coupons;
    private final CouponTemplateGateway templates;
    @Override public PromotionQuoteDTO calculate(PromotionCalculateBo input) {
        if(input==null || input.items()==null) throw new IllegalArgumentException("报价请求不能为空");
        var member=members.requireActive(input.memberId());
        var coupon=input.couponId()==null?null:coupons.coupon(input.memberId(),input.couponId(),false);
        if(input.couponId()!=null && coupon==null) throw new IllegalArgumentException("优惠券不属于该会员");
        return PromotionQuoteConverter.to(pricing.price(input.items().stream().map(PromotionQuoteConverter::from).toList(),coupon,templates.now(),member.discountPercent(),input.pointStatus(),member.points()==null?0L:member.points()));
    }
    @Override public ActivityQuoteDTO priceActivity(ActivityPriceBo input) {
        if(input==null || input.lines()==null) throw new IllegalArgumentException("活动报价请求不能为空");
        var lines=input.lines().stream().map(v->new PromotionOrderLineCmd(v.skuId(),v.quantity())).toList();
        var cmd=PromotionOrderCmd.batch(input.memberId(),input.activityType(),input.activityId(),input.recordId(),input.headId(),lines,null,input.requestNo());
        var result=activities.price(cmd);
        return new ActivityQuoteDTO(result.activityType(),result.activityId(),result.recordId(),result.headId(),result.skuId(),result.quantity(),result.activityPrice(),result.usePoint(),PromotionQuoteConverter.to(result.quote()),result.lines().stream().map(v->new ActivityQuoteDTO.Line(v.skuId(),v.quantity(),v.activityPrice(),v.usePoint())).toList());
    }
}
