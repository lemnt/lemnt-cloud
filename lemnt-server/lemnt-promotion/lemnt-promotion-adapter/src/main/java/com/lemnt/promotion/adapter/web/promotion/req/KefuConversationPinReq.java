package com.lemnt.promotion.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 客服会话置顶请求。
 */
@Schema(description = "客服会话置顶请求")
public record KefuConversationPinReq(@Schema(description = "是否置顶") @NotNull Boolean pinned)
    implements Serializable {
}
