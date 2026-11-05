package com.lemnt.order.infrastructure.order.gateway;

import com.lemnt.order.domain.order.gateway.OrderCommentGateway;
import com.lemnt.order.domain.order.model.VerifiedOrderComment;
import com.lemnt.product.client.ProductCommentClient;
import com.lemnt.product.client.bo.ProductCommentSaveBo;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 私有商城评价RPC，写请求不自动重试，原请求由上层显式核验。
 */
@Component
public class OrderCommentGatewayImpl implements OrderCommentGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private ProductCommentClient client;

    @Override
    public Long saveVerified(VerifiedOrderComment value) {
        return save(value, false);
    }

    @Override
    public Long saveSystem(VerifiedOrderComment value) {
        return save(value, true);
    }

    private Long save(VerifiedOrderComment value, boolean system) {
        var bo =
            new ProductCommentSaveBo(
                value.memberId(),
                value.orderNo(),
                value.skuId(),
                value.orderId(),
                value.orderItemId(),
                value.spuId(),
                value.spuName(),
                value.skuPicUrl(),
                value.specs() == null
                    ? null
                    : value.specs().stream()
                    .map(
                        property ->
                            new ProductCommentSaveBo.Spec(
                                property.specId(),
                                property.specName(),
                                property.specValueId(),
                                property.specValueName()))
                    .toList(),
                value.uniqueSku(),
                value.allowCreate(),
                value.score(),
                value.content(),
                value.descriptionScores(),
                value.benefitScores(),
                value.anonymous(),
                value.picUrls(),
                value.userNickname(),
                value.userAvatar());
        return system ? client.saveSystem(bo) : client.saveVerified(bo);
    }
}
