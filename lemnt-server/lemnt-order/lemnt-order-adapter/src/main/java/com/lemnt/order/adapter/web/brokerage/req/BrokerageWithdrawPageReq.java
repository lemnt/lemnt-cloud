package com.lemnt.order.adapter.web.brokerage.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 提现单分页请求。 */
@Schema(description = "提现单分页请求")
public record BrokerageWithdrawPageReq(
        @Schema(description = "会员编号") Long userId,
        @Schema(description = "状态") Integer status,
        @Schema(description = "pageNum") Integer pageNum,
        @Schema(description = "pageSize") Integer pageSize)
        implements Serializable {}
