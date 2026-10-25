package com.lemnt.trade.adapter.web.fulfillment.req;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "RefundRequest请求")
public record RefundRequestReq(
        @Schema(description = "幂等请求编号") @NotBlank @Size(max = 64) String requestNo,
        @Schema(description = "申请原因") @NotBlank @Size(max = 500) String reason)
        implements Serializable {}
