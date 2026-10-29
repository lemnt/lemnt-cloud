package com.lemnt.trade.client.bo;
/** 商城委托交易按真实订单项完成评价，不含可由调用方设置的评价状态。 */
public record OrderCommentCreateBo(Long memberId, String orderNo, Long skuId, Long orderItemId, Integer score, String content, Integer descriptionScores, Integer benefitScores, Boolean anonymous, java.util.List<String> picUrls) implements java.io.Serializable {}
