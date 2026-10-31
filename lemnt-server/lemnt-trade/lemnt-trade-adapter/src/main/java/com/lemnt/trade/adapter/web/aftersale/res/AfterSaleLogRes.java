package com.lemnt.trade.adapter.web.aftersale.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 后台售后操作日志响应。 */
@Schema(description = "售后操作日志")
public record AfterSaleLogRes(
        @Schema(description = "日志编号") Long id,
        @Schema(description = "操作者编号，系统为0") Long userId,
        @Schema(description = "系统0、会员1、后台2") Integer userType,
        @Schema(description = "售后主键") Long afterSaleId,
        @Schema(description = "操作前状态，新建时可缺失") Integer beforeStatus,
        @Schema(description = "操作后状态：10申请、20同意退货、30寄回、40待退款、50完成、61取消、62拒绝、63拒收") Integer afterStatus,
        @Schema(description = "芋道售后操作类型编号") Integer operateType,
        @Schema(description = "操作说明") String content,
        @Schema(description = "数据库时区发生时间") LocalDateTime createTime) implements Serializable {
}
