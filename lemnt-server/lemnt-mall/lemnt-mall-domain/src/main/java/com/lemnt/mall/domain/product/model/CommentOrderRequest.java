package com.lemnt.mall.domain.product.model;
/** 请求交易服务核验购买事实并编排评价。 */
public record CommentOrderRequest(Long memberId, String orderNo, Long skuId, Long orderItemId, Integer score, String content, Integer descriptionScores, Integer benefitScores, Boolean anonymous, java.util.List<String> picUrls) {}
