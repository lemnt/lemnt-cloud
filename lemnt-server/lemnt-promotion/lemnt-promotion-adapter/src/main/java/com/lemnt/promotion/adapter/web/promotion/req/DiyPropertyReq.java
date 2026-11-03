package com.lemnt.promotion.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 装修属性更新请求，property 为 JSON 文本。 */
@Schema(description = "装修属性更新请求")
public record DiyPropertyReq(
        @Schema(description = "JSON 属性") @NotBlank @Size(max = 60000) String property)
        implements java.io.Serializable {}
