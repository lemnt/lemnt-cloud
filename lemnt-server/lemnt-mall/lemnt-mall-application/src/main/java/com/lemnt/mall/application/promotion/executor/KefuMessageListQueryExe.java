package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.KefuChatService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 管理端客服消息查询执行器。 */
@Component
@RequiredArgsConstructor
public class KefuMessageListQueryExe {
    private final KefuChatService service;

    public java.util.List<com.lemnt.mall.application.promotion.dto.KefuMessageDTO> execute(
            com.lemnt.mall.application.promotion.qry.KefuMessageQry qry) {
        return service.adminMessages(qry);
    }
}
