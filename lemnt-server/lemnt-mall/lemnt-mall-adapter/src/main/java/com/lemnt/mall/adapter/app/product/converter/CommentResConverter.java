package com.lemnt.mall.adapter.app.product.converter;

import com.lemnt.mall.adapter.app.product.res.CommentPageRes;

import com.lemnt.mall.adapter.app.product.res.CommentCreateRes;
import com.lemnt.mall.application.product.dto.ProductCommentDTO;

/** 会员评价创建响应转换。 */
public final class CommentResConverter {
    private CommentResConverter() {
    }

    /** 返回稳定的评价编号。 */
    public static CommentCreateRes created(ProductCommentDTO value) {
        return new CommentCreateRes(value.id());
    }

    /** 匿名输出不携带保存时头像，不暴露后台标识。 */
    public static CommentPageRes page(ProductCommentDTO value) {
        boolean anonymous = Boolean.TRUE.equals(value.anonymous());
        return new CommentPageRes(value.id(), value.spuId(), value.skuId(),
                anonymous ? "匿名用户" : value.userNickname(), anonymous ? null : value.userAvatar(), value.anonymous(),
                value.score(), value.descriptionScores(), value.benefitScores(), value.content(), value.picUrls(),
                value.spuName(), value.skuPicUrl(), value.skuProperties() == null ? null : value.skuProperties().stream().map(property ->
                        new CommentPageRes.Property(property.propertyId(), property.propertyName(), property.valueId(), property.valueName())).toList(),
                value.replyStatus(), value.replyContent(), value.replyTime(), value.createTime());
    }
}
