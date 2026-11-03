package com.lemnt.product.application.product.dto;

/**
 * 分类数据传输对象。
 */
public record CategoryDTO(
        Long id, Long parentId, String name, String picUrl, Integer sort, String status)
        implements java.io.Serializable {}
