package com.lemnt.promotion.application.promotion.cmd;

import java.io.Serializable;

/**
 * 客服消息发送命令，发送人由服务端会话确定。
 */
public record KefuMessageSendCmd(
    Long conversationId, Long senderId, String content, Integer contentType)
    implements Serializable {
}
