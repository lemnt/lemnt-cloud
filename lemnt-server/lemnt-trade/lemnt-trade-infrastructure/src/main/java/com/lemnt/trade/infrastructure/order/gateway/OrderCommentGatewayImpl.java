package com.lemnt.trade.infrastructure.order.gateway;
import com.lemnt.trade.domain.order.gateway.OrderCommentGateway;
import com.lemnt.trade.domain.order.model.VerifiedOrderComment;
import com.lemnt.mall.client.ProductCommentClient;
import com.lemnt.mall.client.bo.ProductCommentSaveBo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
/** 私有商城评价RPC，写请求不自动重试，原请求由上层显式核验。 */
@Component
public class OrderCommentGatewayImpl implements OrderCommentGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private ProductCommentClient client;
    @Override
    public Long saveVerified(VerifiedOrderComment value) {
        return client.saveVerified(new ProductCommentSaveBo(value.memberId(), value.orderNo(), value.skuId(), value.orderId(), value.orderItemId(), value.spuId(),
                value.spuName(), value.skuPicUrl(), value.properties() == null ? null : value.properties().stream().map(property ->
                        new ProductCommentSaveBo.Property(property.propertyId(), property.propertyName(), property.valueId(), property.valueName())).toList(),
                value.uniqueSku(), value.allowCreate(), value.score(), value.content(), value.descriptionScores(), value.benefitScores(), value.anonymous(), value.picUrls()));
    }
}
