package com.lemnt.order.adapter.web.brokerage.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 分销资格开关请求。
 */
@Schema(description = "分销资格开关请求")
public record BrokerageEnabledReq(@Schema(description = "是否开通") @NotNull Boolean enabled)
    implements Serializable {
}
