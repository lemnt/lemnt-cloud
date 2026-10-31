package com.lemnt.mall.adapter.web.product.res;
import io.swagger.v3.oas.annotations.media.Schema;
/** 商品评价显隐接口独立结果。 */
public record CommentVisibleRes(@Schema(description = "是否修改成功") boolean success) {}
