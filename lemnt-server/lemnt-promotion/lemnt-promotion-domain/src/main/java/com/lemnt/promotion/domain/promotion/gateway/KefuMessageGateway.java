package com.lemnt.promotion.domain.promotion.gateway;

import com.lemnt.promotion.domain.promotion.model.KefuMessage;

import java.time.LocalDateTime;
import java.util.List;

/** 客服消息存储契约。 */
public interface KefuMessageGateway {
    KefuMessage save(KefuMessage value);

    /** 按时间倒序查询最近消息，beforeTime 用于历史翻页。 */
    List<KefuMessage> list(Long conversationId, LocalDateTime beforeTime, int limit);

    /** 将对方发送的未读消息标记为已读。 */
    boolean markRead(Long conversationId, int readerType);

    long unreadCount(Long conversationId, int readerType);
}
