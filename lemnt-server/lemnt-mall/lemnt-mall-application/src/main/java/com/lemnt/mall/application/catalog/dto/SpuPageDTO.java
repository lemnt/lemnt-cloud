package com.lemnt.mall.application.catalog.dto;

import java.util.List;

/**
 * 商品SPU分页数据传输对象。
 */
public record SpuPageDTO(
        List<SpuDTO> records,
        long total
) implements java.io.Serializable {}
