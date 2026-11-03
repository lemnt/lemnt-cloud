package com.lemnt.order.adapter.app.brokerage.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 会员佣金记录/提现分页请求。 */
@Schema(description = "会员佣金记录/提现分页请求")
public record AppBrokeragePageReq(
        @Schema(description = "状态") Integer status,
        @Schema(description = "pageNum") Integer pageNum,
        @Schema(description = "pageSize") Integer pageSize)
        implements Serializable {}
