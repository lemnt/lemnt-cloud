package com.lemnt.pay.adapter.web.payment.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "按实际到账时间汇总的每日支付情况")
public record PaymentTrendDayRes(
        @Schema(description = "到账日期") String date,
        @Schema(description = "成功订单支付笔数") long payments,
        @Schema(description = "订单支付毛额，元") String paid,
        @Schema(description = "历史成功充值实付毛额，元，含后来已退款充值") String recharged)
        implements Serializable {}
