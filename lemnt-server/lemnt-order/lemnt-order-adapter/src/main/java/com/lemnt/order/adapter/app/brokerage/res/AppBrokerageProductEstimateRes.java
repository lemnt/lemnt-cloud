package com.lemnt.order.adapter.app.brokerage.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "一级单件商品佣金预估")
public record AppBrokerageProductEstimateRes(
        @Schema(description = "当前会员可查看且存在可预估的在售规格") boolean enabled,
        @Schema(description = "最低一级单件佣金，元") String brokerageMinPrice,
        @Schema(description = "最高一级单件佣金，元") String brokerageMaxPrice)
        implements Serializable {}
