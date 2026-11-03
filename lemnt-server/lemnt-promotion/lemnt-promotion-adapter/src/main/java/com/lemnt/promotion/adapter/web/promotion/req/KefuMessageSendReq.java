package com.lemnt.promotion.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 管理端客服回复请求。 */
@Schema(description = "管理端客服回复请求")
public record KefuMessageSendReq(
        @Schema(description = "消息类型：1文本、2图片") @NotNull Integer contentType,
        @Schema(description = "消息内容或图片地址") @NotBlank @Size(max = 2000) String content)
        implements Serializable {}
