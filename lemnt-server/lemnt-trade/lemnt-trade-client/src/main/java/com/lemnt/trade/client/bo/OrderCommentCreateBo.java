package com.lemnt.trade.client.bo;

import java.io.Serializable;

import java.util.List;

/** 商城委托交易按真实订单项完成评价，不含可由调用方设置的评价状态。 */
public record OrderCommentCreateBo(
        Long memberId,
        String orderNo,
        Long skuId,
        Long orderItemId,
        Integer score,
        String content,
        Integer descriptionScores,
        Integer benefitScores,
        Boolean anonymous,
        List<String> picUrls
) implements Serializable {}
