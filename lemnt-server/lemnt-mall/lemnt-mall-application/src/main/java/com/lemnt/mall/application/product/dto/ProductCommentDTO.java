package com.lemnt.mall.application.product.dto;

import java.util.List;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 商品评价与保存时资料；历史缺失字段保持null。 */
public record ProductCommentDTO(
        Long id,
        Long spuId,
        Long skuId,
        String orderNo,
        Long memberId,
        Integer score,
        String content,
        boolean visible,
        String replyContent,
        Long replyUserId,
        LocalDateTime replyTime,
        LocalDateTime createTime,
        Integer descriptionScores,
        Integer benefitScores,
        List<String> picUrls,
        Boolean anonymous,
        String userNickname,
        String userAvatar,
        Long orderId,
        String spuName,
        String skuPicUrl,
        List<Property> skuProperties,
        boolean replyStatus,
        Long orderItemId
) implements Serializable {
    /** 历史评价缺少细分评分与会员商品快照，不反推或补当前资料。 */
    public ProductCommentDTO(Long id, Long spuId, Long skuId, String orderNo, Long memberId, Integer score, String content, boolean visible, String replyContent, Long replyUserId, LocalDateTime replyTime, LocalDateTime createTime) {
        this(id, spuId, skuId, orderNo, memberId, score, content, visible, replyContent, replyUserId, replyTime, createTime, null, null, null, null, null, null, null, null, null, null, replyContent != null, null);
    }

    public ProductCommentDTO {
        picUrls = picUrls == null ? null : List.copyOf(picUrls);
        skuProperties = skuProperties == null ? null : List.copyOf(skuProperties);
    }

    /** 保存时的规格值，不关联当前字典进行回填。 */
    public record Property(Long propertyId, String propertyName, Long valueId, String valueName) implements Serializable {}
}
