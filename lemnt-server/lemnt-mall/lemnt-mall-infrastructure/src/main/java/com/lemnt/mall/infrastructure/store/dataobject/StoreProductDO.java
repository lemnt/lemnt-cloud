package com.lemnt.mall.infrastructure.store.dataobject;

@lombok.Data
public class StoreProductDO {
    private Long id;
    private String name;
    private Long categoryId;
    private Long brandId;
    private String image;
    private java.math.BigDecimal minPrice;
    private java.math.BigDecimal maxPrice;
    private boolean available;
}
