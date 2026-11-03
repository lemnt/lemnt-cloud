package com.lemnt.product.infrastructure.order.gateway;

import com.lemnt.product.domain.order.gateway.OrderSnapshotGateway;
import com.lemnt.product.domain.product.model.CommentOrderRequest;
import com.lemnt.trade.client.TradeOrderClient;
import com.lemnt.trade.client.bo.OrderCommentCreateBo;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 过渡期消费旧订单Client；P4切换新Client，绝不依赖订单实现模块。 */
@Component
public class OrderSnapshotGatewayImpl implements OrderSnapshotGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private TradeOrderClient client;

    @Override
    public Long createComment(CommentOrderRequest value) {
        return client.createComment(
                new OrderCommentCreateBo(
                        value.memberId(),
                        value.orderNo(),
                        value.skuId(),
                        value.orderItemId(),
                        value.score(),
                        value.content(),
                        value.descriptionScores(),
                        value.benefitScores(),
                        value.anonymous(),
                        value.picUrls()));
    }
}
