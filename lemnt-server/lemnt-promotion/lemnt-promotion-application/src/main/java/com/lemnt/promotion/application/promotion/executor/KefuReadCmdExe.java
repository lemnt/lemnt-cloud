package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.service.KefuChatService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 管理端客服消息已读执行器。 */
@Component
@RequiredArgsConstructor
public class KefuReadCmdExe {
    private final KefuChatService service;

    public Boolean execute(Long id) {
        return service.adminRead(id);
    }
}
