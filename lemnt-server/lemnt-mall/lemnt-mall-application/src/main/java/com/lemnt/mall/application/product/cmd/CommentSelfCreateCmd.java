package com.lemnt.mall.application.product.cmd;
/** 后台自评输入，不携带购买关系或会员身份。 */
public record CommentSelfCreateCmd(String userNickname, String userAvatar, Long skuId, Integer descriptionScores, Integer benefitScores, String content, java.util.List<String> picUrls) {}
