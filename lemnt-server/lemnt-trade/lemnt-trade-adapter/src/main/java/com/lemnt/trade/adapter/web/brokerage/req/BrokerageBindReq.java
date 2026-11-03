package com.lemnt.trade.adapter.web.brokerage.req;

import io.swagger.v3.oas.annotations.media.Schema;

/** 管理端修改推广员请求，bindUserId 为空表示清除。 */
@Schema(description = "管理端修改推广员请求")
public record BrokerageBindReq(@Schema(description = "推广员编号，空表示清除") Long bindUserId)
        implements java.io.Serializable {}
