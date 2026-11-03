package com.lemnt.promotion.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 管理端客服会话。 */
@Schema(description = "管理端客服会话")
public record KefuConversationRes(
        @Schema(description = "会话编号") Long id,
        @Schema(description = "会员编号") Long userId,
        @Schema(description = "会员昵称") String nickname,
        @Schema(description = "会员头像") String avatar,
        @Schema(description = "最后消息时间") LocalDateTime lastMessageTime,
        @Schema(description = "最后消息") String lastMessageContent,
        @Schema(description = "最后消息类型") Integer lastMessageContentType,
        @Schema(description = "管理端置顶") Boolean adminPinned,
        @Schema(description = "管理员未读消息数") Integer adminUnreadMessageCount)
        implements java.io.Serializable {}
