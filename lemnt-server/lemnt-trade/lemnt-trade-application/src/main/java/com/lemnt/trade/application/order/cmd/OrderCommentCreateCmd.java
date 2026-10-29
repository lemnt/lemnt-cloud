package com.lemnt.trade.application.order.cmd;

import java.util.List;

/** 交易侧评价编排命令，身份来自已验证的会员上下文。 */
public record OrderCommentCreateCmd(
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
) {}
