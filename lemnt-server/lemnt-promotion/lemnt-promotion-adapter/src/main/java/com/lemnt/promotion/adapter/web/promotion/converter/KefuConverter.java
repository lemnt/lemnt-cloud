package com.lemnt.promotion.adapter.web.promotion.converter;

import com.lemnt.promotion.adapter.web.promotion.req.KefuMessageSendReq;
import com.lemnt.promotion.adapter.web.promotion.res.KefuConversationRes;
import com.lemnt.promotion.adapter.web.promotion.res.KefuMessageRes;
import com.lemnt.promotion.application.promotion.cmd.KefuMessageSendCmd;
import com.lemnt.promotion.application.promotion.dto.KefuConversationDTO;
import com.lemnt.promotion.application.promotion.dto.KefuMessageDTO;
import com.lemnt.promotion.application.promotion.qry.KefuMessageQry;

import java.time.LocalDateTime;

/**
 * 管理端客服请求及响应转换。
 */
public final class KefuConverter {
    private KefuConverter() {
    }

    public static KefuMessageSendCmd send(
        Long conversationId, Long adminId, KefuMessageSendReq req) {
        return new KefuMessageSendCmd(conversationId, adminId, req.content(), req.contentType());
    }

    public static KefuMessageQry messages(
        Long conversationId, LocalDateTime beforeTime, Integer limit) {
        return new KefuMessageQry(conversationId, beforeTime, limit);
    }

    public static KefuConversationRes conversation(KefuConversationDTO value) {
        return new KefuConversationRes(
            value.id(),
            value.userId(),
            value.nickname(),
            value.avatar(),
            value.lastMessageTime(),
            value.lastMessageContent(),
            value.lastMessageContentType(),
            value.adminPinned(),
            value.adminUnreadMessageCount());
    }

    public static KefuMessageRes message(KefuMessageDTO value) {
        return new KefuMessageRes(
            value.id(),
            value.conversationId(),
            value.senderId(),
            value.senderType(),
            value.contentType(),
            value.content(),
            value.readStatus(),
            value.createTime());
    }
}
