package com.lemnt.product.adapter.app.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;

import java.io.Serializable;

/**
 * 公开评价查询，不能传visible/会员身份或后台条件。
 */
@Schema(description = "商品评价分页查询")
public record ProductCommentPageReq(
    @Schema(description = "SPU编号") @NotNull @Positive Long spuId,
    @Schema(description = "0全部、1好评、2中评、3差评") @NotNull @Min(0) @Max(3) Integer type,
    @Schema(description = "页码默认1") @Min(1) @Max(1000000) Integer pageNum,
    @Schema(description = "每页默认20，最多100") @Min(1) @Max(100) Integer pageSize)
    implements Serializable {
}
