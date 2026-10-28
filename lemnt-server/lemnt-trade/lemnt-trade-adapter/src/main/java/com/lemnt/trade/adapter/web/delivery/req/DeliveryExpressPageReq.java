package com.lemnt.trade.adapter.web.delivery.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 配送分页请求。 */
@Schema(description = "配送分页请求")
public record DeliveryExpressPageReq(
        @Schema(description = "名称模糊查询")
        @Size(max = 100)
        String name,
        @Schema(description = "0 启用、1 停用")
        @Min(0) @Max(1)
        Integer status,
        @Schema(description = "页码，默认 1")
        @Min(1)
        Integer pageNum,
        @Schema(description = "每页数量，默认 20，上限 100")
        @Min(1) @Max(100)
        Integer pageSize
) implements Serializable {
    public DeliveryExpressPageReq {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 20 : pageSize;
    }
}
