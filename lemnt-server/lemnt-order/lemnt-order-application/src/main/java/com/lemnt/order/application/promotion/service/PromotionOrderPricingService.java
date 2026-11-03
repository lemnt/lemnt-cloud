package com.lemnt.order.application.promotion.service;
import com.lemnt.order.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.order.application.promotion.dto.PromotionOrderDraft;
import com.lemnt.order.application.settlement.service.SettlementDeliveryService;
import com.lemnt.order.domain.product.gateway.ProductCatalogGateway;
import com.lemnt.order.domain.promotion.gateway.PromotionPricingGateway;
import com.lemnt.order.domain.promotion.model.ActivityPricingRequest;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
/** 营销提供活动新报价，配送在订单本域计算，随后保存原报价供重试。 */
@Component
@RequiredArgsConstructor
public class PromotionOrderPricingService {
    private final PromotionPricingGateway pricing;
    private final SettlementDeliveryService delivery;
    private final ProductCatalogGateway products;
    public PromotionOrderDraft price(PromotionOrderCmd cmd) {
        if(cmd==null)throw new IllegalArgumentException("活动下单请求不能为空");
        var result=pricing.priceActivity(new ActivityPricingRequest(cmd.memberId(),cmd.requestNo(),cmd.activityType(),cmd.activityId(),cmd.recordId(),cmd.headId(),cmd.lines().stream().map(v->new ActivityPricingRequest.Line(v.skuId(),v.quantity())).toList()));
        var quote=result.quote();
        if(cmd.addressId()!=null) quote=delivery.attach(cmd.memberId(),cmd.addressId(),products.findOnShelfBatch(cmd.lines().stream().map(v->v.skuId()).toList()),quote);
        return new PromotionOrderDraft(cmd,result.activityType(),result.activityId(),result.recordId(),result.headId(),result.skuId(),result.quantity(),result.activityPrice(),result.usePoint(),quote,result.lines().stream().map(v->new PromotionOrderDraft.Line(v.skuId(),v.quantity(),v.activityPrice(),v.usePoint())).toList());
    }
}
