package com.lemnt.trade.adapter.web.pickup.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/** 自提门店绑定核销店员请求。 */
@Schema(description = "自提门店绑定核销店员请求")
public record PickUpBindReq(@Schema(description = "管理员编号") @NotNull List<Long> verifyUserIds)
        implements java.io.Serializable {}
