package com.lemnt.order.infrastructure.promotion.gateway;
import com.lemnt.order.domain.promotion.gateway.PromotionPricingGateway;
import com.lemnt.order.domain.promotion.model.ActivityPricingRequest;
import com.lemnt.order.domain.promotion.model.ActivityPricingResult;
import com.lemnt.order.domain.settlement.model.SettlementItem;
import com.lemnt.order.domain.settlement.model.SettlementQuote;
import com.lemnt.order.infrastructure.promotion.converter.PromotionQuoteConverter;
import com.lemnt.promotion.client.PromotionCalcClient;
import com.lemnt.promotion.client.bo.ActivityPriceBo;
import com.lemnt.promotion.client.bo.PromotionCalculateBo;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;
/** 显式映射报价行、优惠来源及分摊，不使用JSON往返丢弃新增字段。 */
@Component
public class PromotionPricingGatewayImpl implements PromotionPricingGateway {
    @DubboReference(check=false,timeout=5000,retries=0) private PromotionCalcClient client;
    public SettlementQuote price(Long member,List<SettlementItem> items,Long coupon,boolean point) {
        return PromotionQuoteConverter.from(client.calculate(new PromotionCalculateBo(member,null,items.stream().map(PromotionQuoteConverter::to).toList(),coupon,point)));
    }
    public ActivityPricingResult priceActivity(ActivityPricingRequest input) {
        var result=client.priceActivity(new ActivityPriceBo(input.memberId(),input.requestNo(),input.activityType(),input.activityId(),input.recordId(),input.headId(),input.lines().stream().map(v->new ActivityPriceBo.Line(v.skuId(),v.quantity())).toList()));
        return new ActivityPricingResult(result.activityType(),result.activityId(),result.recordId(),result.headId(),result.skuId(),result.quantity(),result.activityPrice(),result.usePoint(),PromotionQuoteConverter.from(result.quote()),result.lines().stream().map(v->new ActivityPricingResult.Line(v.skuId(),v.quantity(),v.activityPrice(),v.usePoint())).toList());
    }
}
