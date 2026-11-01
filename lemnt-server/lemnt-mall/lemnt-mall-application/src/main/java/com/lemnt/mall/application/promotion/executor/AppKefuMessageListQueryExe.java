package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.KefuChatService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 小程序本人客服消息查询执行器。 */
@Component
@RequiredArgsConstructor
public class AppKefuMessageListQueryExe {
    private final KefuChatService service;

    public java.util.List<com.lemnt.mall.application.promotion.dto.KefuMessageDTO> execute(
            com.lemnt.mall.application.promotion.qry.AppKefuMessageQry qry) {
        return service.appMessages(qry);
    }
}
