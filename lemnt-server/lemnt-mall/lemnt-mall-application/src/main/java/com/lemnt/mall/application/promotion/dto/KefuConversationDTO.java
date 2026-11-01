package com.lemnt.mall.application.promotion.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 客服会话输出，管理端带会员昵称和头像。 */
public record KefuConversationDTO(
        Long id,
        Long userId,
        String nickname,
        String avatar,
        LocalDateTime lastMessageTime,
        String lastMessageContent,
        Integer lastMessageContentType,
        Boolean adminPinned,
        Integer adminUnreadMessageCount
) implements Serializable {
}
