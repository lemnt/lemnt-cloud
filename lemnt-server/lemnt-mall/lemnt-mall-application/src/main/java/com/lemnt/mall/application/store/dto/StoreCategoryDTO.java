package com.lemnt.mall.application.store.dto;

/**
 * 门店分类数据传输对象。
 */
public record StoreCategoryDTO(
        Long id,
        Long parentId,
        String name,
        String picUrl
) implements java.io.Serializable {}
