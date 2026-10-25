package com.lemnt.mall.infrastructure.catalog.dataobject;

import lombok.Data;

@Data
public class SkuPropertyDO {
    private Long productId;
    private String propertyName;
    private String propertyValue;
}
