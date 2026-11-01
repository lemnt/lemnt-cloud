package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.KefuChatService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 管理端客服会话详情执行器。 */
@Component
@RequiredArgsConstructor
public class KefuConversationDetailQueryExe {
    private final KefuChatService service;

    public com.lemnt.mall.application.promotion.dto.KefuConversationDTO execute(Long id) {
        return service.adminDetail(id);
    }
}
