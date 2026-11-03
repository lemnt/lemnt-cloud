package com.lemnt.trade.adapter.web.brokerage.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/** 管理端分销用户。 */
@Schema(description = "管理端分销用户")
public record BrokerageUserRes(
        @Schema(description = "会员编号") Long id,
        @Schema(description = "推广员编号") Long bindUserId,
        @Schema(description = "推广员昵称") String bindNickname,
        @Schema(description = "推广员头像") String bindAvatar,
        @Schema(description = "是否有分销资格") Boolean brokerageEnabled,
        @Schema(description = "可用佣金，元") BigDecimal brokeragePrice,
        @Schema(description = "冻结佣金，元") BigDecimal frozenPrice,
        @Schema(description = "一级推广人数") Long firstLevelCount,
        @Schema(description = "二级推广人数") Long secondLevelCount)
        implements Serializable {}
