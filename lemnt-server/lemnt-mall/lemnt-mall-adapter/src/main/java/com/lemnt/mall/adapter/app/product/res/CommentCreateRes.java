package com.lemnt.mall.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 会员评价创建响应。 */
@Schema(description = "会员评价创建响应")
public record CommentCreateRes(
        @Schema(description = "已创建的真实评价ID")
        Long id
) implements Serializable {
}
