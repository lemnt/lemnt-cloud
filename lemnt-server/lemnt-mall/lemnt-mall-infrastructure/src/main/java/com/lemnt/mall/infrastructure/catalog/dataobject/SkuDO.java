package com.lemnt.mall.infrastructure.catalog.dataobject;

import lombok.Data;

@Data
public class SkuDO {
    private Long id;
    private Long spuId;
    private String code;
    private String name;
    private java.math.BigDecimal price;
    private Integer stock;
    private String image;
    private String status;
    private String variantKey;
}
