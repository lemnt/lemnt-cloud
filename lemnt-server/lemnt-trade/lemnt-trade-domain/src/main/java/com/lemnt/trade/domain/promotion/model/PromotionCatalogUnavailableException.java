package com.lemnt.trade.domain.promotion.model;

public final class PromotionCatalogUnavailableException extends IllegalStateException {
    public PromotionCatalogUnavailableException() {
        super("CATALOG_FACTS_UNAVAILABLE");
    }
}
