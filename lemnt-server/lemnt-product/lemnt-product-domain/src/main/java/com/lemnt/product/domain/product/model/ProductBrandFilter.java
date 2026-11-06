package com.lemnt.product.domain.product.model;

import java.io.Serializable;

/**
 * 品牌分页条件。
 */
public record ProductBrandFilter(String name, String status, int pageNum, int pageSize)
    implements Serializable {
}
