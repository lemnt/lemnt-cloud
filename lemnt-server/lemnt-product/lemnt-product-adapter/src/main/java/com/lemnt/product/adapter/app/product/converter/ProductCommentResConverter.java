package com.lemnt.product.adapter.app.product.converter;

import com.lemnt.product.adapter.app.product.res.ProductCommentPageRes;
import com.lemnt.product.application.product.dto.ProductCommentDTO;

/**
 * 会员评价创建响应转换。
 */
public final class ProductCommentResConverter {
    private ProductCommentResConverter() {
    }


    /**
     * 匿名输出不携带保存时头像，不暴露后台标识。
     */
    public static ProductCommentPageRes page(ProductCommentDTO value) {
        boolean anonymous = Boolean.TRUE.equals(value.anonymous());
        return new ProductCommentPageRes(
            value.id(),
            value.spuId(),
            value.skuId(),
            anonymous ? "匿名用户" : value.userNickname(),
            anonymous ? null : value.userAvatar(),
            value.anonymous(),
            value.score(),
            value.descriptionScores(),
            value.benefitScores(),
            value.content(),
            value.picUrls(),
            value.spuName(),
            value.skuPicUrl(),
            value.skuProperties() == null
                ? null
                : value.skuProperties().stream()
                .map(
                    property ->
                        new ProductCommentPageRes.Spec(
                            property.specId(),
                            property.specName(),
                            property.specValueId(),
                            property.specValueName()))
                .toList(),
            value.replyStatus(),
            value.replyContent(),
            value.replyTime(),
            value.createTime());
    }
}
