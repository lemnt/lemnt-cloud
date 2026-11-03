package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.service.KefuChatService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 小程序本人客服消息查询执行器。 */
@Component
@RequiredArgsConstructor
public class AppKefuMessageListQueryExe {
    private final KefuChatService service;

    public java.util.List<com.lemnt.promotion.application.promotion.dto.KefuMessageDTO> execute(
            com.lemnt.promotion.application.promotion.qry.AppKefuMessageQry qry) {
        return service.appMessages(qry);
    }
}
