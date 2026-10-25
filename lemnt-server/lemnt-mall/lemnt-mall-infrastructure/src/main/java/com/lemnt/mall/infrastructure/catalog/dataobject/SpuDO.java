package com.lemnt.mall.infrastructure.catalog.dataobject;

import lombok.Data;

@Data
public class SpuDO {
    private Long id;
    private String code;
    private String name;
    private Long categoryId;
    private Long brandId;
    private String description;
    private String image;
    private String status;
}
