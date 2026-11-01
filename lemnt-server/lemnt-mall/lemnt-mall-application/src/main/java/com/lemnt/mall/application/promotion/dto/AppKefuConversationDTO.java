package com.lemnt.mall.application.promotion.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 小程序本人客服会话输出。 */
public record AppKefuConversationDTO(
        Long id,
        LocalDateTime lastMessageTime,
        String lastMessageContent,
        Integer lastMessageContentType,
        Long unreadCount
) implements Serializable {
}
