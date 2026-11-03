package com.lemnt.promotion.infrastructure.promotion.converter;

import com.lemnt.promotion.domain.promotion.model.KefuConversation;
import com.lemnt.promotion.domain.promotion.model.KefuMessage;
import com.lemnt.promotion.infrastructure.promotion.dataobject.KefuConversationDO;
import com.lemnt.promotion.infrastructure.promotion.dataobject.KefuMessageDO;

/** 客服领域对象与表结构转换。 */
public final class KefuConverter {
    private KefuConverter() {}

    public static KefuConversation model(KefuConversationDO row) {
        if (row == null) {
            return null;
        }
        return new KefuConversation(
                row.getId(),
                row.getUserId(),
                row.getLastMessageTime(),
                row.getLastMessageContent(),
                row.getLastMessageContentType(),
                row.getAdminPinned(),
                row.getAdminDeleted(),
                row.getAdminUnreadMessageCount());
    }

    public static KefuConversationDO row(KefuConversation value) {
        var row = new KefuConversationDO();
        row.setId(value.id());
        row.setUserId(value.userId());
        row.setLastMessageTime(value.lastMessageTime());
        row.setLastMessageContent(value.lastMessageContent());
        row.setLastMessageContentType(value.lastMessageContentType());
        row.setAdminPinned(value.adminPinned());
        row.setAdminDeleted(value.adminDeleted());
        row.setAdminUnreadMessageCount(value.adminUnreadMessageCount());
        return row;
    }

    public static KefuMessage model(KefuMessageDO row) {
        if (row == null) {
            return null;
        }
        return new KefuMessage(
                row.getId(),
                row.getConversationId(),
                row.getSenderId(),
                row.getSenderType(),
                row.getReceiverId(),
                row.getReceiverType(),
                row.getContentType(),
                row.getContent(),
                row.getReadStatus(),
                row.getCreateTime());
    }

    public static KefuMessageDO row(KefuMessage value) {
        var row = new KefuMessageDO();
        row.setId(value.id());
        row.setConversationId(value.conversationId());
        row.setSenderId(value.senderId());
        row.setSenderType(value.senderType());
        row.setReceiverId(value.receiverId());
        row.setReceiverType(value.receiverType());
        row.setContentType(value.contentType());
        row.setContent(value.content());
        row.setReadStatus(value.readStatus());
        row.setCreateTime(value.createTime());
        return row;
    }
}
