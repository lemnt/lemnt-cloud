package com.lemnt.trade.adapter.web.checkout.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Schema(description = "CheckoutSubmitReq")
public record CheckoutSubmitReq(
        @Schema(description = "该会员内唯一幂等请求编号，同编号必须用于相同请求") @NotBlank @Size(max = 64)
                String requestNo,
        @Schema(description = "该会员购物车中已选中的SKU编号，1至100项") @NotEmpty @Size(max = 100)
                java.util.List<@NotNull @Positive Long> productIds,
        @Schema(description = "该会员的优惠券实例编号，可不传") @Positive Long couponId,
        @Schema(description = "服务器预览返回的报价令牌，价格或数量变化会拒绝下单")
                @NotBlank
                @Pattern(regexp = "[a-f0-9]{64}")
                String quoteToken)
        implements Serializable {}
