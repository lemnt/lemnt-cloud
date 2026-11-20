package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.cmd.AppKefuMessageSendCmd;
import com.lemnt.promotion.application.promotion.service.KefuChatService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 小程序客服消息发送执行器。
 */
@Component
@RequiredArgsConstructor
public class AppKefuMessageSendCmdExe {
    private final KefuChatService service;

    public Long execute(AppKefuMessageSendCmd cmd) {
        return service.appSend(cmd);
    }
}
