package com.lemnt.promotion.application.promotion.cmd;

import java.io.Serializable;

/**
 * 小程序客服消息发送命令。
 */
public record AppKefuMessageSendCmd(Long memberId, String content, Integer contentType)
    implements Serializable {
}
