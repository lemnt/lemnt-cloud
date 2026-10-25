package com.lemnt.mall.infrastructure.catalog.dataobject;

import lombok.Data;

@Data
public class BrandDO {
    private Long id;
    private String name;
    private String picUrl;
    private String description;
    private Integer sort;
    private String status;
}
