package com.lemnt.product.application.product.converter;

import com.lemnt.product.application.product.dto.ProductCommentDTO;
import com.lemnt.product.domain.product.model.ProductComment;

/**
 * 评价领域记录与应用输出转换。
 */
public final class CommentDTOConverter {
    private CommentDTOConverter() {
    }

    /**
     * 转换完整评价与商家回复。
     */
    public static ProductCommentDTO toDTO(ProductComment value) {
        return value == null
            ? null
            : new ProductCommentDTO(
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
                        new ProductCommentDTO.Spec(
                            property.specId(),
                            property.specName(),
                            property.specValueId(),
                            property.specValueName()))
                .toList(),
            value.replyStatus(),
            value.orderItemId());
    }
}
