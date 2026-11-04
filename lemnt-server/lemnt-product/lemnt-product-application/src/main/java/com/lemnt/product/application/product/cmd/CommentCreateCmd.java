package com.lemnt.product.application.product.cmd;

import java.io.Serializable;
import java.util.List;

/**
 * 会员评价创建。
 */
public record CommentCreateCmd(
    Long memberId,
    String orderNo,
    Long skuId,
    Integer score,
    String content,
    Integer descriptionScores,
    Integer benefitScores,
    Boolean anonymous,
    List<String> picUrls,
    Long orderItemId)
    implements Serializable {
    /**
     * 兼容上一版多维评分客户端，旧SKU定位由交易校验唯一性。
     */
    public CommentCreateCmd(
        Long memberId,
        String orderNo,
        Long skuId,
        Integer score,
        String content,
        Integer descriptionScores,
        Integer benefitScores,
        Boolean anonymous,
        List<String> picUrls) {
        this(
            memberId,
            orderNo,
            skuId,
            score,
            content,
            descriptionScores,
            benefitScores,
            anonymous,
            picUrls,
            null);
    }

    /**
     * 兼容旧客户端单项评分输入，明确视为两项相同分数。
     */
    public CommentCreateCmd(
        Long memberId, String orderNo, Long skuId, Integer score, String content) {
        this(memberId, orderNo, skuId, score, content, null, null, false, List.of());
    }
}
