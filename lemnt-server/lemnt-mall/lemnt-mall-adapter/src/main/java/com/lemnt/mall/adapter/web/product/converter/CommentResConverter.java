package com.lemnt.mall.adapter.web.product.converter;

import com.lemnt.mall.adapter.web.product.res.CommentDetailRes;
import com.lemnt.mall.adapter.web.product.res.CommentPageRes;
import com.lemnt.mall.adapter.web.product.res.CommentReplyRes;
import com.lemnt.mall.application.product.dto.ProductCommentDTO;

/** 后台评价独立响应转换。 */
public final class CommentResConverter {
    private CommentResConverter() {
    }

    /** 转换分页接口响应。 */
    public static CommentPageRes page(ProductCommentDTO value) {
        return new CommentPageRes(value.id(), value.spuId(), value.skuId(), value.orderNo(), value.memberId(),
            value.score(), value.content(), value.visible(), value.replyContent(), value.replyUserId(), value.replyTime(), value.createTime(), value.descriptionScores(), value.benefitScores(), value.picUrls(),
                value.anonymous(), value.userNickname(), value.userAvatar(), value.orderId(), value.spuName(), value.skuPicUrl(),
                value.skuProperties() == null ? null : value.skuProperties().stream().map(property ->
                        new CommentPageRes.Property(property.propertyId(), property.propertyName(), property.valueId(), property.valueName())).toList(),
                value.replyStatus());
    }

    /** 转换详情接口响应。 */
    public static CommentDetailRes detail(ProductCommentDTO value) {
        return new CommentDetailRes(value.id(), value.spuId(), value.skuId(), value.orderNo(), value.memberId(),
            value.score(), value.content(), value.visible(), value.replyContent(), value.replyUserId(), value.replyTime(), value.createTime(), value.descriptionScores(), value.benefitScores(), value.picUrls(),
                value.anonymous(), value.userNickname(), value.userAvatar(), value.orderId(), value.spuName(), value.skuPicUrl(),
                value.skuProperties() == null ? null : value.skuProperties().stream().map(property ->
                        new CommentDetailRes.Property(property.propertyId(), property.propertyName(), property.valueId(), property.valueName())).toList(),
                value.replyStatus());
    }

    /** 转换回复接口响应。 */
    public static CommentReplyRes reply(ProductCommentDTO value) {
        return new CommentReplyRes(value.id(), value.spuId(), value.skuId(), value.orderNo(), value.memberId(),
            value.score(), value.content(), value.visible(), value.replyContent(), value.replyUserId(), value.replyTime(), value.createTime(), value.descriptionScores(), value.benefitScores(), value.picUrls(),
                value.anonymous(), value.userNickname(), value.userAvatar(), value.orderId(), value.spuName(), value.skuPicUrl(),
                value.skuProperties() == null ? null : value.skuProperties().stream().map(property ->
                        new CommentReplyRes.Property(property.propertyId(), property.propertyName(), property.valueId(), property.valueName())).toList(),
                value.replyStatus());
    }
}
