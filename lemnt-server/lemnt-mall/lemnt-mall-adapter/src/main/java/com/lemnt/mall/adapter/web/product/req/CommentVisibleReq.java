package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/** 评价显示设置请求。 */
@Schema(description = "评价显示设置请求")
public record CommentVisibleReq(
        @Schema(description = "是否展示")
        @NotNull
        Boolean visible
) implements Serializable {
}
