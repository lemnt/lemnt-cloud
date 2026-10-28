package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 商品评价分页响应。 */
@Schema(description = "商品评价分页响应")
public record CommentPageRes(
        @Schema(description = "评价ID")
        Long id,
        @Schema(description = "SPU编号")
        Long spuId,
        @Schema(description = "SKU编号")
        Long skuId,
        @Schema(description = "原订单编号")
        String orderNo,
        @Schema(description = "真实评价会员ID")
        Long memberId,
        @Schema(description = "评分，1至5")
        Integer score,
        @Schema(description = "评价内容")
        String content,
        @Schema(description = "是否显示")
        boolean visible,
        @Schema(description = "商家回复，可空")
        String replyContent,
        @Schema(description = "回复管理员ID，可空")
        Long replyUserId,
        @Schema(description = "回复时间，可空")
        LocalDateTime replyTime,
        @Schema(description = "评价时间")
        LocalDateTime createTime
) implements Serializable {
}
