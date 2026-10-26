package com.lemnt.mall.infrastructure.promotion.dataobject;

@lombok.Data
public class PricingFactsDO {
    private Long skuId;
    private Long spuId;
    private String productCode;
    private String name;
    private java.math.BigDecimal price;
    private Integer stock;
}
