package com.lemnt.trade.adapter.web.order.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 后台订单操作日志响应。 */
@Schema(description = "订单操作日志")
public record OrderLogRes(
        @Schema(description = "日志编号") Long id,
        @Schema(description = "操作者编号，系统为0") Long userId,
        @Schema(description = "系统0、会员1、后台2") Integer userType,
        @Schema(description = "订单主键") Long orderId,
        @Schema(description = "操作前状态，新建时可缺失") Integer beforeStatus,
        @Schema(description = "操作后状态：0待支付、10待发货、20待收货、30完成、40取消") Integer afterStatus,
        @Schema(description = "芋道订单操作类型编号") Integer operateType,
        @Schema(description = "操作说明") String content,
        @Schema(description = "数据库时区发生时间") LocalDateTime createTime) implements Serializable {
}
