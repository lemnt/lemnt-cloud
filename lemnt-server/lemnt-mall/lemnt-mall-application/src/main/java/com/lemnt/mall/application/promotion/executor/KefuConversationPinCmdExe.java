package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.KefuConversationPinCmd;
import com.lemnt.mall.application.promotion.service.KefuChatService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 管理端客服会话置顶执行器。 */
@Component
@RequiredArgsConstructor
public class KefuConversationPinCmdExe {
    private final KefuChatService service;

    public Boolean execute(KefuConversationPinCmd cmd) {
        return service.adminPin(cmd);
    }
}
