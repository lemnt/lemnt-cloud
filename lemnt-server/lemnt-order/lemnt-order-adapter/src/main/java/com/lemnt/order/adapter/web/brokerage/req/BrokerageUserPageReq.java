package com.lemnt.order.adapter.web.brokerage.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 分销用户分页请求。 */
@Schema(description = "分销用户分页请求")
public record BrokerageUserPageReq(
        @Schema(description = "推广员编号") Long bindUserId,
        @Schema(description = "是否有分销资格") Boolean brokerageEnabled,
        @Schema(description = "pageNum") Integer pageNum,
        @Schema(description = "pageSize") Integer pageSize)
        implements Serializable {}
