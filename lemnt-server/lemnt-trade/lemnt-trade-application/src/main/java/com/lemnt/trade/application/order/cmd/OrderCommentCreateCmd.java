package com.lemnt.trade.application.order.cmd;
/** 交易侧评价编排命令，身份来自已验证的会员上下文。 */
public record OrderCommentCreateCmd(Long memberId, String orderNo, Long skuId, Long orderItemId, Integer score, String content, Integer descriptionScores, Integer benefitScores, Boolean anonymous, java.util.List<String> picUrls) {}
