package com.lemnt.promotion.application.promotion.cmd;

import java.io.Serializable;

/** 客服会话置顶命令。 */
public record KefuConversationPinCmd(Long id, Boolean pinned) implements Serializable {}
