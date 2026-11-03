package com.lemnt.promotion.domain.promotion.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 客服消息：senderType 1 会员、2 管理员。 */
public record KefuMessage(
        Long id,
        Long conversationId,
        Long senderId,
        Integer senderType,
        Long receiverId,
        Integer receiverType,
        Integer contentType,
        String content,
        Boolean readStatus,
        LocalDateTime createTime)
        implements Serializable {}
