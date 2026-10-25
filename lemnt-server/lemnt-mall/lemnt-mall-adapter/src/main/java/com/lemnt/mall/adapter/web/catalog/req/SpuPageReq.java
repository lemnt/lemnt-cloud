package com.lemnt.mall.adapter.web.catalog.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Schema(description = "SpuPageReq")
public record SpuPageReq(
        @Schema(description = "商品名称包含匹配") @Size(max = 200) String name,
        @Schema(description = "ON_SHELF或OFF_SHELF，可不传") String status,
        @Schema(description = "页码，默认1") @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页条数，默认10，最大100") @Min(1) @Max(100) Integer pageSize)
        implements Serializable {
    public SpuPageReq {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
    }
}
