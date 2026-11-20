package com.lemnt.promotion.domain.promotion.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客服会话：一个会员一个会话，保存最后消息与管理员未读数。
 */
public record KefuConversation(
    Long id,
    Long userId,
    LocalDateTime lastMessageTime,
    String lastMessageContent,
    Integer lastMessageContentType,
    Boolean adminPinned,
    Boolean adminDeleted,
    Integer adminUnreadMessageCount)
    implements Serializable {
}
