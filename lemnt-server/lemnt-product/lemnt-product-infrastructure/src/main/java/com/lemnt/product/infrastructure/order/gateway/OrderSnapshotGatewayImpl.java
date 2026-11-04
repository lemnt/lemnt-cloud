package com.lemnt.product.infrastructure.order.gateway;

import com.lemnt.order.client.TradeOrderClient;
import com.lemnt.order.client.bo.OrderCommentCreateBo;
import com.lemnt.product.domain.order.gateway.OrderSnapshotGateway;
import com.lemnt.product.domain.product.model.CommentOrderRequest;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 通过独立订单Client提交评价授权核验，绝不依赖订单实现模块。 */
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
