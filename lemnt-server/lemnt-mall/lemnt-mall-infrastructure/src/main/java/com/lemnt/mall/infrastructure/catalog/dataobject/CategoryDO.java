package com.lemnt.mall.infrastructure.catalog.dataobject;

import lombok.Data;

@Data
public class CategoryDO {
    private Long id;
    private Long parentId;
    private String name;
    private String picUrl;
    private Integer sort;
    private String status;
}
