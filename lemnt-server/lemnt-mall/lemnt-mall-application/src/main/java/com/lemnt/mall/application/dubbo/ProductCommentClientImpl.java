package com.lemnt.mall.application.dubbo;
import com.lemnt.mall.client.ProductCommentClient;
import com.lemnt.mall.client.bo.ProductCommentSaveBo;
import com.lemnt.mall.application.product.cmd.CommentSaveCmd;
import com.lemnt.mall.application.product.service.ProductCommentService;
import org.apache.dubbo.config.annotation.DubboService;
import lombok.RequiredArgsConstructor;
/** 只接收交易已验证的购买事实，不在私有保存时回查交易订单。 */
@DubboService
@RequiredArgsConstructor
public class ProductCommentClientImpl implements ProductCommentClient {
    private final ProductCommentService comments;
    @Override
    public Long saveVerified(ProductCommentSaveBo value) {
        return save(value, false);
    }
    @Override
    public Long saveSystem(ProductCommentSaveBo value) { return save(value, true); }
    private Long save(ProductCommentSaveBo value, boolean system) {
        var cmd = new CommentSaveCmd(value.memberId(), value.orderNo(), value.skuId(), value.orderId(), value.orderItemId(), value.spuId(),
                value.spuName(), value.skuPicUrl(), value.specs() == null ? null : value.specs().stream().map(property ->
                        new CommentSaveCmd.Spec(property.specId(), property.specName(), property.specValueId(), property.specValueName())).toList(),
                value.uniqueSku(), value.allowCreate(), value.score(), value.content(), value.descriptionScores(), value.benefitScores(), value.anonymous(), value.picUrls());
        return (system ? comments.saveSystem(cmd) : comments.saveVerified(cmd)).id();
    }
}
