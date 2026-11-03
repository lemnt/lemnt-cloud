package com.lemnt.mall.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 商品收藏移除响应。 */
@Schema(description = "商品收藏移除响应")
public record AppProductFavoriteDeleteRes(@Schema(description = "取消是否成功") boolean success)
        implements Serializable {}
