package com.lemnt.mall.client.bo;

import java.io.Serializable;

import java.util.List;

/**
 * 交易服务从真实订单项生成的可信购买事实；不作为HTTP请求体。
 * @param uniqueSku 由交易确认该SKU在订单中唯一，供旧评价绑定使用
 * @param allowCreate 未评项可以首次创建，已评项只能核验原记录
 */
public record ProductCommentSaveBo(
        Long memberId,
        String orderNo,
        Long skuId,
        Long orderId,
        Long orderItemId,
        Long spuId,
        String spuName,
        String skuPicUrl,
        List<Property> properties,
        boolean uniqueSku,
        boolean allowCreate,
        Integer score,
        String content,
        Integer descriptionScores,
        Integer benefitScores,
        Boolean anonymous,
        List<String> picUrls
) implements Serializable {
    /** 原订单规格，不从当前字典补造。 */
    public record Property(
            Long propertyId,
            String propertyName,
            Long valueId,
            String valueName
    ) implements Serializable {}
}
