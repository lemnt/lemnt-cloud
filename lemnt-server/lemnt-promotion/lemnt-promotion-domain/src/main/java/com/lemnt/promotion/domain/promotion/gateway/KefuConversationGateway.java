package com.lemnt.promotion.domain.promotion.gateway;

import com.lemnt.promotion.domain.promotion.model.KefuConversation;

import java.time.LocalDateTime;
import java.util.List;

/** 客服会话存储契约。 */
public interface KefuConversationGateway {
    KefuConversation findByUserId(Long userId);

    KefuConversation find(Long id);

    KefuConversation save(KefuConversation value);

    /** 管理端可见会话，置顶优先、最后消息时间倒序。 */
    List<KefuConversation> listVisible();

    /** 更新最后消息；会员发送时管理员未读数加一，并恢复被管理员删除的会话。 */
    boolean updateLastMessage(
            Long id, LocalDateTime time, String content, Integer contentType, boolean fromMember);

    boolean clearAdminUnread(Long id);

    boolean setPinned(Long id, boolean pinned);

    boolean setAdminDeleted(Long id, boolean deleted);
}
