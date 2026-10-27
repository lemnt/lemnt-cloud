package com.lemnt.mall.application.store.dto;

import java.util.List;

/**
 * 门店分页数据传输对象。
 */
public record StorePageDTO(
        List<StoreProductDTO> records,
        long total
) implements java.io.Serializable {}
