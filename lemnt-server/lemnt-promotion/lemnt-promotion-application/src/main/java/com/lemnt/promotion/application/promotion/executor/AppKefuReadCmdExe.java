package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.service.KefuChatService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 小程序客服消息已读执行器。
 */
@Component
@RequiredArgsConstructor
public class AppKefuReadCmdExe {
    private final KefuChatService service;

    public Boolean execute(Long memberId) {
        return service.appRead(memberId);
    }
}
