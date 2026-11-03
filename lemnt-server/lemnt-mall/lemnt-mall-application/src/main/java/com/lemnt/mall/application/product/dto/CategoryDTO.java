package com.lemnt.mall.application.product.dto;

import java.io.Serializable;

/**
 * 分类数据传输对象。
 */
public record CategoryDTO(
        Long id, Long parentId, String name, String picUrl, Integer sort, String status)
        implements Serializable {}
