package com.lemnt.order.adapter.web.order.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 后台订单改址请求。
 */
@Schema(description = "后台订单改址请求")
public record OrderAdjustAddressReq(
    @Schema(description = "收件人姓名") @NotBlank @Size(max = 100) String receiverName,
    @Schema(description = "收件人电话") @NotBlank @Pattern(regexp = "[+0-9() -]{5,32}")
    String receiverMobile,
    @Schema(description = "收件地区编号") @NotNull @Positive Long receiverAreaId,
    @Schema(description = "收件详细地址") @NotBlank @Size(max = 500) String receiverDetailAddress)
    implements Serializable {
}
