package com.lemnt.mall.client.bo;
/** 交易服务从真实订单项生成的可信购买事实；不作为HTTP请求体。 */
public record ProductCommentSaveBo(Long memberId, String orderNo, Long skuId, Long orderId, Long orderItemId, Long spuId, String spuName, String skuPicUrl, java.util.List<Property> properties, boolean uniqueSku, boolean allowCreate, Integer score, String content, Integer descriptionScores, Integer benefitScores, Boolean anonymous, java.util.List<String> picUrls) implements java.io.Serializable {
    /** 原订单规格，不从当前字典补造。 */
    public record Property(Long propertyId, String propertyName, Long valueId, String valueName) implements java.io.Serializable {}
}
