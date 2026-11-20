package com.lemnt.product.application.product.qry;

import java.io.Serializable;

/**
 * 启用品牌选择查询。
 */
public record ProductBrandSimpleListQry(String name, int limit) implements Serializable {
}
