package com.lemnt.mall.adapter.app.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 会员真实订单评价请求。 */
@Schema(description = "会员真实订单评价请求")
public record CommentCreateReq(
        @Schema(description = "本人已完成订单编号")
        @NotBlank
        @Size(max = 40)
        String orderNo,
        @Schema(description = "订单内SKU编号")
        @NotNull
        @Positive
        Long skuId,
        @Schema(description = "评分，1至5")
        @NotNull
        @Min(1)
        @Max(5)
        Integer score,
        @Schema(description = "评价正文")
        @NotBlank
        @Size(max = 1000)
        String content
) implements Serializable {
}
