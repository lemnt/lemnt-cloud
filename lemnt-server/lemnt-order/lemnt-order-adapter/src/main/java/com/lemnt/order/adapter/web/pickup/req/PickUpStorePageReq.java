package com.lemnt.order.adapter.web.pickup.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 自提门店分页请求。
 */
@Schema(description = "自提门店分页请求")
public record PickUpStorePageReq(
    @Schema(description = "门店名称") String name,
    @Schema(description = "状态") Integer status,
    @Schema(description = "pageNum") Integer pageNum,
    @Schema(description = "pageSize") Integer pageSize)
    implements Serializable {
}
