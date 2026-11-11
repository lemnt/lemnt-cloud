package com.lemnt.order.domain.promotion.gateway;

import com.lemnt.order.domain.order.model.TradeOrderItem;
import com.lemnt.order.domain.promotion.model.OrderGiftAdjustment;
import com.lemnt.order.domain.promotion.model.OrderPromotion;
import com.lemnt.order.domain.promotion.model.OrderPromotionRefund;

import java.util.List;

/**
 * 订单优惠结果及退单归因的本地持久化边界。
 */
public interface OrderPromotionGateway {
    /**
     * 订单与商品行写入后，在相同本地事务保存优惠及真实明细主键映射。
     */
    void save(Long orderId, List<OrderPromotion> values, List<TradeOrderItem> items);

    /**
     * 读取原始优惠快照，不查询当前活动。
     */
    List<OrderPromotion> find(Long orderId);

    /**
     * 生成退单时写入本次确定的优惠归因。
     */
    void saveRefund(Long afterSaleId, List<OrderPromotionRefund> values);

    /**
     * 按明确售后主键批量读取其冻结归因，调用方筛选成功状态。
     */
    List<OrderPromotionRefund> refunds(List<Long> afterSaleIds);

    /**
     * 调价与赠积分结果使用同一trade事务更新，只允许尚未支付的订单。
     */
    void updateGift(OrderGiftAdjustment adjustment);
}
