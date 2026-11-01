package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.KefuChatService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 管理端客服回复执行器。 */
@Component
@RequiredArgsConstructor
public class KefuMessageSendCmdExe {
    private final KefuChatService service;

    public Long execute(com.lemnt.mall.application.promotion.cmd.KefuMessageSendCmd cmd) {
        return service.adminSend(cmd);
    }
}
