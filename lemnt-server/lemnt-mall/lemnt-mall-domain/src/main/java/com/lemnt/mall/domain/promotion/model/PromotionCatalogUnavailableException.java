package com.lemnt.mall.domain.promotion.model;

/**
 * 营销目录暂不可用异常。
 */
public final class PromotionCatalogUnavailableException extends IllegalStateException {
    /**
     * 创建营销目录暂不可用异常实例。
     */
    public PromotionCatalogUnavailableException() {
        super("CATALOG_FACTS_UNAVAILABLE");
    }
}
