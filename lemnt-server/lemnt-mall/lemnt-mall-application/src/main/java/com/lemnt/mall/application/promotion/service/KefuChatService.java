package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.promotion.cmd.AppKefuMessageSendCmd;
import com.lemnt.mall.application.promotion.cmd.KefuConversationPinCmd;
import com.lemnt.mall.application.promotion.cmd.KefuMessageSendCmd;
import com.lemnt.mall.application.promotion.dto.AppKefuConversationDTO;
import com.lemnt.mall.application.promotion.dto.KefuConversationDTO;
import com.lemnt.mall.application.promotion.dto.KefuMessageDTO;
import com.lemnt.mall.application.promotion.qry.AppKefuMessageQry;
import com.lemnt.mall.application.promotion.qry.KefuMessageQry;
import com.lemnt.mall.domain.member.gateway.MemberLookupGateway;
import com.lemnt.mall.domain.promotion.gateway.KefuConversationGateway;
import com.lemnt.mall.domain.promotion.gateway.KefuMessageGateway;
import com.lemnt.mall.domain.promotion.model.KefuConversation;
import com.lemnt.mall.domain.promotion.model.KefuMessage;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 客服会话编排：会员端自动建会话，管理端回复并维护未读数。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KefuChatService {
    public static final int USER_TYPE_MEMBER = 1;
    public static final int USER_TYPE_ADMIN = 2;
    public static final int CONTENT_TEXT = 1;
    public static final int CONTENT_IMAGE = 2;

    private final KefuConversationGateway conversations;
    private final KefuMessageGateway messages;
    private final MemberLookupGateway members;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    private KefuConversation conversation(Long id) {
        var value = conversations.find(id);
        require(value != null, "客服会话不存在");
        return value;
    }

    private String content(String value, Integer contentType) {
        require(
                contentType != null
                        && (contentType == CONTENT_TEXT || contentType == CONTENT_IMAGE),
                "消息类型只能为文本或图片");
        require(value != null && !value.isBlank() && value.length() <= 2000, "消息不能为空且不超过2000字");
        String text = value.trim();
        if (contentType == CONTENT_IMAGE) {
            require(
                    text.length() <= 500
                            && (text.startsWith("https://") || text.startsWith("http://")),
                    "图片消息必须是http或https地址");
        }
        return text;
    }

    private int limit(Integer value) {
        int limit = value == null ? 10 : value;
        require(limit >= 1 && limit <= 200, "每次查询条数须在1至200之间");
        return limit;
    }

    private KefuConversation getOrCreate(Long memberId) {
        var value = conversations.findByUserId(memberId);
        if (value != null) {
            return value;
        }
        return conversations.save(
                new KefuConversation(
                        null, memberId, LocalDateTime.now(), "", CONTENT_TEXT, false, false, 0));
    }

    public AppKefuConversationDTO appConversation(Long memberId) {
        require(memberId != null && memberId > 0, "会员编号不合法");
        var value = conversations.findByUserId(memberId);
        if (value == null) {
            return null;
        }
        return new AppKefuConversationDTO(
                value.id(),
                value.lastMessageTime(),
                value.lastMessageContent(),
                value.lastMessageContentType(),
                messages.unreadCount(value.id(), USER_TYPE_MEMBER));
    }

    /** 消息按时间正序返回，方便客户端直接追加展示。 */
    public List<KefuMessageDTO> appMessages(AppKefuMessageQry qry) {
        require(qry != null && qry.memberId() != null && qry.memberId() > 0, "会员编号不合法");
        var conversation = conversations.findByUserId(qry.memberId());
        if (conversation == null) {
            return List.of();
        }
        return dto(messages.list(conversation.id(), qry.beforeTime(), limit(qry.limit())));
    }

    public Long appSend(AppKefuMessageSendCmd cmd) {
        require(cmd != null && cmd.memberId() != null && cmd.memberId() > 0, "会员编号不合法");
        String text = content(cmd.content(), cmd.contentType());
        var conversation = getOrCreate(cmd.memberId());
        var now = LocalDateTime.now();
        var saved =
                messages.save(
                        new KefuMessage(
                                null,
                                conversation.id(),
                                cmd.memberId(),
                                USER_TYPE_MEMBER,
                                0L,
                                USER_TYPE_ADMIN,
                                cmd.contentType(),
                                text,
                                false,
                                now));
        require(
                conversations.updateLastMessage(
                        conversation.id(), now, text, cmd.contentType(), true),
                "客服会话消息更新失败");
        return saved.id();
    }

    public Boolean appRead(Long memberId) {
        require(memberId != null && memberId > 0, "会员编号不合法");
        var conversation = conversations.findByUserId(memberId);
        if (conversation != null) {
            messages.markRead(conversation.id(), USER_TYPE_MEMBER);
        }
        return true;
    }

    public List<KefuConversationDTO> adminList() {
        return conversations.listVisible().stream().map(this::conversationDto).toList();
    }

    public KefuConversationDTO adminDetail(Long id) {
        require(id != null && id > 0, "客服会话编号不合法");
        return conversationDto(conversation(id));
    }

    public List<KefuMessageDTO> adminMessages(KefuMessageQry qry) {
        require(
                qry != null && qry.conversationId() != null && qry.conversationId() > 0,
                "客服会话编号不合法");
        conversation(qry.conversationId());
        return dto(messages.list(qry.conversationId(), qry.beforeTime(), limit(qry.limit())));
    }

    public Long adminSend(KefuMessageSendCmd cmd) {
        require(
                cmd != null
                        && cmd.conversationId() != null
                        && cmd.conversationId() > 0
                        && cmd.senderId() != null
                        && cmd.senderId() > 0,
                "客服消息参数不合法");
        String text = content(cmd.content(), cmd.contentType());
        var conversation = conversation(cmd.conversationId());
        var now = LocalDateTime.now();
        var saved =
                messages.save(
                        new KefuMessage(
                                null,
                                conversation.id(),
                                cmd.senderId(),
                                USER_TYPE_ADMIN,
                                conversation.userId(),
                                USER_TYPE_MEMBER,
                                cmd.contentType(),
                                text,
                                false,
                                now));
        require(
                conversations.updateLastMessage(
                        conversation.id(), now, text, cmd.contentType(), false),
                "客服会话消息更新失败");
        return saved.id();
    }

    public Boolean adminRead(Long id) {
        conversation(id);
        messages.markRead(id, USER_TYPE_ADMIN);
        conversations.clearAdminUnread(id);
        return true;
    }

    public Boolean adminPin(KefuConversationPinCmd cmd) {
        require(cmd != null && cmd.id() != null && cmd.id() > 0 && cmd.pinned() != null, "置顶参数不合法");
        conversation(cmd.id());
        require(conversations.setPinned(cmd.id(), cmd.pinned()), "客服会话置顶失败");
        return true;
    }

    public Boolean adminDelete(Long id) {
        conversation(id);
        require(conversations.setAdminDeleted(id, true), "客服会话删除失败");
        return true;
    }

    private KefuConversationDTO conversationDto(KefuConversation value) {
        var member = members.findById(value.userId());
        return new KefuConversationDTO(
                value.id(),
                value.userId(),
                member == null ? null : member.nickname(),
                member == null ? null : member.avatar(),
                value.lastMessageTime(),
                value.lastMessageContent(),
                value.lastMessageContentType(),
                value.adminPinned(),
                value.adminUnreadMessageCount());
    }

    private List<KefuMessageDTO> dto(List<KefuMessage> values) {
        var result = new ArrayList<KefuMessageDTO>(values.size());
        for (int index = values.size() - 1; index >= 0; index--) {
            var value = values.get(index);
            result.add(
                    new KefuMessageDTO(
                            value.id(),
                            value.conversationId(),
                            value.senderId(),
                            value.senderType(),
                            value.contentType(),
                            value.content(),
                            value.readStatus(),
                            value.createTime()));
        }
        return result;
    }
}
