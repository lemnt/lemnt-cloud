package com.lemnt.product.adapter.web.product.converter;

import com.lemnt.product.adapter.web.product.res.ProductCommentDetailRes;
import com.lemnt.product.adapter.web.product.res.ProductCommentPageRes;
import com.lemnt.product.adapter.web.product.res.ProductCommentReplyRes;
import com.lemnt.product.application.product.dto.ProductCommentDTO;

/**
 * 后台评价独立响应转换。
 */
public final class ProductCommentResConverter {
    private ProductCommentResConverter() {
    }

    /**
     * 转换分页接口响应。
     */
    public static ProductCommentPageRes page(ProductCommentDTO value) {
        return new ProductCommentPageRes(
            value.id(),
            value.spuId(),
            value.skuId(),
            value.orderNo(),
            value.memberId(),
            value.score(),
            value.content(),
            value.visible(),
            value.replyContent(),
            value.replyUserId(),
            value.replyTime(),
            value.createTime(),
            value.descriptionScores(),
            value.benefitScores(),
            value.picUrls(),
            value.anonymous(),
            value.userNickname(),
            value.userAvatar(),
            value.orderId(),
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
            value.orderItemId());
    }

    /**
     * 转换详情接口响应。
     */
    public static ProductCommentDetailRes detail(ProductCommentDTO value) {
        return new ProductCommentDetailRes(
            value.id(),
            value.spuId(),
            value.skuId(),
            value.orderNo(),
            value.memberId(),
            value.score(),
            value.content(),
            value.visible(),
            value.replyContent(),
            value.replyUserId(),
            value.replyTime(),
            value.createTime(),
            value.descriptionScores(),
            value.benefitScores(),
            value.picUrls(),
            value.anonymous(),
            value.userNickname(),
            value.userAvatar(),
            value.orderId(),
            value.spuName(),
            value.skuPicUrl(),
            value.skuProperties() == null
                ? null
                : value.skuProperties().stream()
                .map(
                    property ->
                        new ProductCommentDetailRes.Spec(
                            property.specId(),
                            property.specName(),
                            property.specValueId(),
                            property.specValueName()))
                .toList(),
            value.replyStatus(),
            value.orderItemId());
    }

    /**
     * 转换回复接口响应。
     */
    public static ProductCommentReplyRes reply(ProductCommentDTO value) {
        return new ProductCommentReplyRes(
            value.id(),
            value.spuId(),
            value.skuId(),
            value.orderNo(),
            value.memberId(),
            value.score(),
            value.content(),
            value.visible(),
            value.replyContent(),
            value.replyUserId(),
            value.replyTime(),
            value.createTime(),
            value.descriptionScores(),
            value.benefitScores(),
            value.picUrls(),
            value.anonymous(),
            value.userNickname(),
            value.userAvatar(),
            value.orderId(),
            value.spuName(),
            value.skuPicUrl(),
            value.skuProperties() == null
                ? null
                : value.skuProperties().stream()
                .map(
                    property ->
                        new ProductCommentReplyRes.Spec(
                            property.specId(),
                            property.specName(),
                            property.specValueId(),
                            property.specValueName()))
                .toList(),
            value.replyStatus(),
            value.orderItemId());
    }
}
