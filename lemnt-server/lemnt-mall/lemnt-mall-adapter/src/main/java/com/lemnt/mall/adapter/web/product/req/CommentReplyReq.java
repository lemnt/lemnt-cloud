package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 商家回复请求。 */
@Schema(description = "商家回复请求")
public record CommentReplyReq(
        @Schema(description = "商家回复正文")
        @NotBlank
        @Size(max = 1000)
        String content
) implements Serializable {
}
