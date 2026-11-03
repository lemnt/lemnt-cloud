package com.lemnt.promotion.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 小程序本人客服会话。 */
@Schema(description = "小程序本人客服会话")
public record AppKefuConversationRes(
        @Schema(description = "会话编号") Long id,
        @Schema(description = "最后消息时间") LocalDateTime lastMessageTime,
        @Schema(description = "最后消息") String lastMessageContent,
        @Schema(description = "最后消息类型") Integer lastMessageContentType,
        @Schema(description = "未读消息数") Long unreadCount)
        implements java.io.Serializable {}
