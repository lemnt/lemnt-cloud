package com.lemnt.mall.adapter.app.store.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 购物车新增请求。
 */
@Schema(description = "CartAddReq请求")
public record CartAddReq(
        @Schema(description = "本次增加数量1至999")
        @Min(1)
        @Max(999)
        int quantity,
        @Schema(description = "会员范围内幂等请求编号")
        @NotBlank
        @Size(max = 64)
        String requestNo
) implements Serializable {}
