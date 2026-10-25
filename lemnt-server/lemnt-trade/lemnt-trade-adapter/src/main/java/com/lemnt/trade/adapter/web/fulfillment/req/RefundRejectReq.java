package com.lemnt.trade.adapter.web.fulfillment.req;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "RefundReject请求")
public record RefundRejectReq(
        @Schema(description = "驳回原因") @NotBlank @Size(max = 500) String reason)
        implements Serializable {}
