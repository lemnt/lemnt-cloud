package com.lemnt.mall.domain.product.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 商品评价与保存时资料；历史缺失字段保持null。 */
public record ProductComment(
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
        java.util.List<String> picUrls,
        Boolean anonymous,
        String userNickname,
        String userAvatar,
        Long orderId,
        String spuName,
        String skuPicUrl,
        java.util.List<Property> skuProperties,
        boolean replyStatus
) implements Serializable {
    /** 历史评价缺少细分评分与会员商品快照，不反推或补当前资料。 */
    public ProductComment(Long id, Long spuId, Long skuId, String orderNo, Long memberId, Integer score, String content, boolean visible, String replyContent, Long replyUserId, LocalDateTime replyTime, LocalDateTime createTime) {
        this(id, spuId, skuId, orderNo, memberId, score, content, visible, replyContent, replyUserId, replyTime, createTime, null, null, null, null, null, null, null, null, null, null, replyContent != null);
    }

    public ProductComment {
        picUrls = picUrls == null ? null : java.util.List.copyOf(picUrls);
        skuProperties = skuProperties == null ? null : java.util.List.copyOf(skuProperties);
    }

    /** 保存时的规格值，不关联当前字典进行回填。 */
    public record Property(Long propertyId, String propertyName, Long valueId, String valueName) implements Serializable {}
}
