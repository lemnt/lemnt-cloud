package com.lemnt.order.adapter.app.order.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 会员评价创建响应。
 */
@Schema(description = "会员评价创建响应")
public record AppOrderCommentCreateRes(@Schema(description = "已创建的真实评价ID") Long id)
    implements Serializable {
}
