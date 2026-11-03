package com.lemnt.promotion.application.promotion.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 客服消息输出实体。 */
public record KefuMessageDTO(
        Long id,
        Long conversationId,
        Long senderId,
        Integer senderType,
        Integer contentType,
        String content,
        Boolean readStatus,
        LocalDateTime createTime)
        implements Serializable {}
