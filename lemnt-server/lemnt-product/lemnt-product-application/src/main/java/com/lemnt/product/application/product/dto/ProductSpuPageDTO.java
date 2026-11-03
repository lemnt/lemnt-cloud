package com.lemnt.product.application.product.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 商品SPU分页数据传输对象。
 */
public record ProductSpuPageDTO(List<ProductSpuDTO> records, long total) implements Serializable {}
