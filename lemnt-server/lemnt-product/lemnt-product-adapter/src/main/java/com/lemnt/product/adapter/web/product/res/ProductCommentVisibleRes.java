package com.lemnt.product.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 商品评价显隐接口独立结果。
 */
@Schema(description = "评价显示状态结果")
public record ProductCommentVisibleRes(@Schema(description = "是否修改成功") boolean success)
    implements Serializable {
}
