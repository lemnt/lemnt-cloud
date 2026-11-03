package com.lemnt.order.application.promotion.service;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.order.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.order.application.settlement.service.SettlementOrderService;
import com.lemnt.order.domain.promotion.gateway.PromotionOrderGateway;
import com.lemnt.order.domain.promotion.model.PromotionOrder;
import com.lemnt.order.domain.settlement.model.SettlementOrder;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/** 活动下单只编排原请求与订单快照，库存和权益由所属服务处理。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionOrderService {
    private final PromotionOrderGateway promotionOrders;
    private final PromotionOrderPreparationService preparation;
    private final SettlementOrderService orders;
    private void require(boolean ok,String message){if(!ok)throw new ServiceException(message,400);}
    /** 本地准备提交后同步调用交易建单，未知结果保留原请求供重试。 */
    public SettlementOrder place(PromotionOrderCmd cmd) {
        require(cmd != null && cmd.memberId() != null && cmd.memberId() > 0, "会员编号不合法");
        require(
                cmd.requestNo() != null && cmd.requestNo().matches("[A-Za-z0-9_-]{1,64}"),
                "请求编号不合法");
        require(cmd.lines() != null && !cmd.lines().isEmpty(), "活动商品不能为空");
        var lineKey =
                cmd.lines().stream()
                        .map(line -> line.skuId() + ":" + line.quantity())
                        .sorted()
                        .toList();
        String fingerprint =
                cmd.lines().size() == 1
                        ? String.join(
                                ":",
                                "PROMOTION",
                                String.valueOf(cmd.activityType()),
                                String.valueOf(cmd.activityId()),
                                String.valueOf(cmd.recordId()),
                                String.valueOf(cmd.headId()),
                                String.valueOf(cmd.skuId()),
                                String.valueOf(cmd.quantity()))
                        : String.join(
                                ":",
                                "PROMOTION",
                                String.valueOf(cmd.activityType()),
                                String.valueOf(cmd.activityId()),
                                String.valueOf(cmd.recordId()),
                                String.valueOf(cmd.headId()),
                                lineKey.toString());
        var prepared = preparation.prepare(cmd, fingerprint);
        try {
            return orders.save(prepared.order());
        } catch (RuntimeException exception) {
            // 交易未生成订单时允许同一请求重试；已生成订单交给 closed 补偿。
            try {
                if (orders.findByOrderNo(prepared.order().orderNo()) == null) {
                    promotionOrders.markFailed(
                            prepared.order().orderNo(),
                            exception.getMessage() == null ? "交易建单失败" : exception.getMessage());
                }
            } catch (RuntimeException probeException) {
                log.warn("活动订单失败状态未能确认, orderNo={}", prepared.order().orderNo(), probeException);
            }
            throw exception;
        }
    }
    public PromotionOrder find(String no){return promotionOrders.findByOrderNo(no);}
}
