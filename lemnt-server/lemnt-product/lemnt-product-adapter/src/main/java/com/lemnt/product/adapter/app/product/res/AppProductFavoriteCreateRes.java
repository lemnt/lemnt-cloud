package com.lemnt.product.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 商品收藏添加响应。
 */
@Schema(description = "商品收藏添加响应")
public record AppProductFavoriteCreateRes(@Schema(description = "收藏记录编号") Long id)
    implements Serializable {
}
