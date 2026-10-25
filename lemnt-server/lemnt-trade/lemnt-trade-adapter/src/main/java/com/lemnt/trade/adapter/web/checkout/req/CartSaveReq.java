package com.lemnt.trade.adapter.web.checkout.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Schema(description = "CartSaveReq")
public record CartSaveReq(
        @Schema(description = "购物数量，1至999") @NotNull @Min(1) @Max(999) Integer quantity,
        @Schema(description = "是否选中") @NotNull Boolean selected)
        implements Serializable {}
