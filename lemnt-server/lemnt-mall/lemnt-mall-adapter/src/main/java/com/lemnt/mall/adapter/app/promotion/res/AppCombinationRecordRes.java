package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** App 拼团记录。 */
@Schema(description = "App 拼团记录")
public record AppCombinationRecordRes(
        @Schema(description = "记录编号")
        Long id,
        @Schema(description = "活动编号")
        Long activityId,
        @Schema(description = "拼团价")
        BigDecimal combinationPrice,
        @Schema(description = "商品 SPU 编号")
        Long spuId,
        @Schema(description = "商品名称")
        String spuName,
        @Schema(description = "商品主图")
        String picUrl,
        @Schema(description = "SKU 编号")
        Long skuId,
        @Schema(description = "购买数量")
        Integer count,
        @Schema(description = "会员编号")
        Long userId,
        @Schema(description = "会员昵称")
        String nickname,
        @Schema(description = "会员头像")
        String avatar,
        @Schema(description = "团长记录编号，0 表示本人是团长")
        Long headId,
        @Schema(description = "0 进行中，1 成功，2 失败")
        Integer status,
        @Schema(description = "订单编号")
        Long orderId,
        @Schema(description = "成团人数")
        Integer userSize,
        @Schema(description = "已参团人数")
        Integer userCount,
        @Schema(description = "是否虚拟成团")
        Boolean virtualGroup,
        @Schema(description = "过期时间")
        LocalDateTime expireTime
) implements java.io.Serializable {
}
