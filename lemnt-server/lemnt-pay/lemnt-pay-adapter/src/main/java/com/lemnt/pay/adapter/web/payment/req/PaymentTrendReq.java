package com.lemnt.pay.adapter.web.payment.req;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Schema(description = "支付到账趋势查询")
public record PaymentTrendReq(
        @Schema(description = "开始日期，包含当天") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull
                LocalDate from,
        @Schema(description = "结束日期，包含当天") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull
                LocalDate to)
        implements Serializable {}
