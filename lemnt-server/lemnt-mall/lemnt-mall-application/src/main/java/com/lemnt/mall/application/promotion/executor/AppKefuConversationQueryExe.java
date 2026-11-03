package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.AppKefuConversationDTO;
import com.lemnt.mall.application.promotion.service.KefuChatService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 小程序本人客服会话查询执行器。 */
@Component
@RequiredArgsConstructor
public class AppKefuConversationQueryExe {
    private final KefuChatService service;

    public AppKefuConversationDTO execute(Long memberId) {
        return service.appConversation(memberId);
    }
}
