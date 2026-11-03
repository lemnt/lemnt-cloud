package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.service.KefuChatService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 管理端客服会话列表执行器。 */
@Component
@RequiredArgsConstructor
public class KefuConversationListQueryExe {
    private final KefuChatService service;

    public java.util.List<com.lemnt.promotion.application.promotion.dto.KefuConversationDTO> execute() {
        return service.adminList();
    }
}
