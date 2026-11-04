package com.lemnt.order.adapter.web.brokerage.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 佣金记录分页请求。
 */
@Schema(description = "佣金记录分页请求")
public record BrokerageRecordPageReq(
    @Schema(description = "会员编号") Long userId,
    @Schema(description = "业务类型") Integer bizType,
    @Schema(description = "状态") Integer status,
    @Schema(description = "pageNum") Integer pageNum,
    @Schema(description = "pageSize") Integer pageSize)
    implements Serializable {
}
