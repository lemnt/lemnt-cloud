package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;

/** 后台商品评价分页请求。 */
@Schema(description = "后台商品评价分页请求")
public record CommentPageReq(
        @Schema(description = "SPU编号，可空")
        @Positive
        Long spuId,
        @Schema(description = "会员ID，可空")
        @Positive
        Long memberId,
        @Schema(description = "评分，可空")
        @Min(1)
        @Max(5)
        Integer score,
        @Schema(description = "显示状态，可空")
        Boolean visible,
        @Schema(description = "是否已回复，可空")
        Boolean replied,
        @Schema(description = "页码，默认1")
        @Min(1)
        @Max(1000000)
        Integer pageNum,
        @Schema(description = "每页数量，默认20，最多100")
        @Min(1)
        @Max(100)
        Integer pageSize
) implements Serializable {
}
