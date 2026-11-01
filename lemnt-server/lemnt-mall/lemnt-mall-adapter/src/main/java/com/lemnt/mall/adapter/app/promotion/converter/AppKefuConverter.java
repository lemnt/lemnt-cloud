package com.lemnt.mall.adapter.app.promotion.converter;

import com.lemnt.mall.adapter.app.promotion.req.AppKefuMessageSendReq;
import com.lemnt.mall.adapter.app.promotion.res.AppKefuConversationRes;
import com.lemnt.mall.adapter.app.promotion.res.AppKefuMessageRes;
import com.lemnt.mall.application.promotion.cmd.AppKefuMessageSendCmd;
import com.lemnt.mall.application.promotion.dto.AppKefuConversationDTO;
import com.lemnt.mall.application.promotion.dto.KefuMessageDTO;
import com.lemnt.mall.application.promotion.qry.AppKefuMessageQry;

import java.time.LocalDateTime;

/** 小程序客服请求及响应转换。 */
public final class AppKefuConverter {
    private AppKefuConverter() {
    }

    public static AppKefuMessageSendCmd send(Long memberId, AppKefuMessageSendReq req) {
        return new AppKefuMessageSendCmd(memberId, req.content(), req.contentType());
    }

    public static AppKefuMessageQry messages(Long memberId, LocalDateTime beforeTime, Integer limit) {
        return new AppKefuMessageQry(memberId, beforeTime, limit);
    }

    public static AppKefuConversationRes conversation(AppKefuConversationDTO value) {
        return value == null ? null : new AppKefuConversationRes(value.id(), value.lastMessageTime(),
                value.lastMessageContent(), value.lastMessageContentType(), value.unreadCount());
    }

    public static AppKefuMessageRes message(KefuMessageDTO value) {
        return new AppKefuMessageRes(value.id(), value.conversationId(), value.senderType(),
                value.contentType(), value.content(), value.readStatus(), value.createTime());
    }
}
