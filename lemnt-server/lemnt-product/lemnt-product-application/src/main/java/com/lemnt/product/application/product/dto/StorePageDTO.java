package com.lemnt.product.application.product.dto;

import java.util.List;

/**
 * 门店分页数据传输对象。
 */
public record StorePageDTO(List<StoreProductDTO> records, long total)
        implements java.io.Serializable {}
