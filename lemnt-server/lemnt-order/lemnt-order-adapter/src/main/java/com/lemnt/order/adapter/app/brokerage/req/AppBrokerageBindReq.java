package com.lemnt.order.adapter.app.brokerage.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/** 会员绑定推广员请求。 */
@Schema(description = "会员绑定推广员请求")
public record AppBrokerageBindReq(@Schema(description = "推广员会员编号") @NotNull Long bindUserId)
        implements Serializable {}
